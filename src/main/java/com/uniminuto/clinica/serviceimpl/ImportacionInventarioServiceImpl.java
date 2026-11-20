package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.InventarioImportacion;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.*;
import com.uniminuto.clinica.repository.InventarioImportacionRepository;
import com.uniminuto.clinica.repository.MedicamentoRepository;
import com.uniminuto.clinica.service.ImportacionFilaProcessor;
import com.uniminuto.clinica.service.ImportacionInventarioService;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.ObjectMapper;
import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;

@Service
public class ImportacionInventarioServiceImpl implements ImportacionInventarioService {
    private static final List<String> HEADERS = List.of("nombre", "tipoProducto", "registroInvima", "precioCompra", "precioVenta",
            "stockMinimo", "numeroLote", "fechaIngreso", "fechaVencimiento", "cantidad");
    private final InventarioImportacionRepository importaciones;
    private final MedicamentoRepository productos;
    private final ImportacionFilaProcessor processor;
    private final ObjectMapper mapper;

    public ImportacionInventarioServiceImpl(InventarioImportacionRepository importaciones, MedicamentoRepository productos,
                                             ImportacionFilaProcessor processor, ObjectMapper mapper) {
        this.importaciones = importaciones; this.productos = productos; this.processor = processor; this.mapper = mapper;
    }

    @Override @Transactional
    public ImportacionPreviewRs prevalidar(MultipartFile file, Long usuarioId) {
        if (file == null || file.isEmpty() || usuarioId == null) throw new BadRequestException("Archivo y usuario autenticado son obligatorios.");
        String filename = Optional.ofNullable(file.getOriginalFilename()).map(n -> Path.of(n).getFileName().toString()).orElse("inventario");
        String extension = filename.toLowerCase(Locale.ROOT);
        if (!extension.endsWith(".csv") && !extension.endsWith(".xlsx")) throw new BadRequestException("Solo se aceptan archivos .csv y .xlsx.");
        try {
            List<List<String>> rows = extension.endsWith(".csv") ? readCsv(file) : readXlsx(file);
            if (rows.size() < 2) throw new BadRequestException("El archivo debe contener encabezados y al menos una fila de datos.");
            if (rows.size() > 10_001) throw new BadRequestException("El archivo excede el límite de 10.000 filas de datos.");
            Map<String, Integer> columns = validateHeaders(rows.get(0));
            List<ImportacionFila> result = new ArrayList<>();
            Set<String> names = new HashSet<>(); Set<String> codes = new HashSet<>();
            for (int i = 1; i < rows.size(); i++) {
                List<String> cells = rows.get(i);
                if (cells.stream().allMatch(String::isBlank)) continue;
                result.add(parseRow(i + 1, cells, columns, names, codes));
            }
            if (result.isEmpty()) throw new BadRequestException("El archivo no contiene filas con datos.");
            int valid = (int) result.stream().filter(ImportacionFila::valida).count();
            InventarioImportacion log = new InventarioImportacion();
            log.setArchivo(filename); log.setUsuarioId(usuarioId); log.setTotalFilas(result.size());
            log.setValidas(valid); log.setInvalidas(result.size() - valid); log.setEstado("PREVALIDADA"); log.setCreadoEn(LocalDateTime.now());
            log.setResultadoJson(mapper.writeValueAsString(result));
            log = importaciones.save(log);
            return new ImportacionPreviewRs(log.getId(), log.getTotalFilas(), valid, result.size() - valid, result);
        } catch (BadRequestException e) { throw e; }
        catch (Exception e) { throw new BadRequestException("No se pudo leer o validar el archivo. Verifique su formato y contenido."); }
    }

    @Override @Transactional
    public ImportacionConfirmRs confirmar(Long id, Long usuarioId) {
        InventarioImportacion log = importaciones.findLockedById(id).orElseThrow(() -> new BadRequestException("La importación no existe."));
        if (!"PREVALIDADA".equals(log.getEstado())) throw new BadRequestException("La importación ya fue confirmada o cerrada.");
        List<ImportacionFila> rows = deserialize(log.getResultadoJson());
        int creadas = 0, fallidas = 0;
        List<ImportacionFila> updated = new ArrayList<>();
        for (ImportacionFila fila : rows) {
            if (!fila.valida()) { updated.add(fila); continue; }
            try { processor.procesar(fila, usuarioId); creadas++; updated.add(fila); }
            catch (RuntimeException e) {
                fallidas++;
                List<String> errors = new ArrayList<>(fila.errores() == null ? List.of() : fila.errores());
                errors.add(e instanceof BadRequestException ? e.getMessage()
                        : "No se pudo guardar la fila por un conflicto de datos; revise nombre, código y lote.");
                updated.add(new ImportacionFila(fila.fila(), fila.producto(), fila.lote(), errors));
            }
        }
        log.setValidas(creadas); log.setInvalidas(log.getInvalidas() + fallidas);
        log.setEstado("CONFIRMADA"); log.setConfirmadoEn(LocalDateTime.now());
        log.setResultadoJson(mapper.writeValueAsString(updated)); importaciones.save(log);
        return new ImportacionConfirmRs(log.getId(), creadas, fallidas + (int) rows.stream().filter(r -> !r.valida()).count(), updated);
    }

    private ImportacionFila parseRow(int rowNum, List<String> cells, Map<String, Integer> columns, Set<String> names, Set<String> codes) {
        List<String> errors = new ArrayList<>();
        try {
            String name = get(cells, columns, "nombre").trim();
            String type = get(cells, columns, "tipoProducto").trim().toUpperCase(Locale.ROOT);
            String code = optional(cells, columns, "codigo");
            String invima = optional(cells, columns, "registroInvima");
            String nameKey = name.toLowerCase(Locale.ROOT);
            if (name.isBlank()) errors.add("nombre es obligatorio.");
            else if (!names.add(nameKey) || productos.existsByNombreIgnoreCase(name)) errors.add("nombre duplicado en archivo o catálogo.");
            if (!type.equals("MEDICAMENTO") && !type.equals("PRODUCTO")) errors.add("tipoProducto debe ser MEDICAMENTO o PRODUCTO.");
            if (type.equals("MEDICAMENTO") && invima.isBlank()) errors.add("registroInvima es obligatorio para medicamentos.");
            if (!code.isBlank()) {
                if (!code.matches("[A-Za-z0-9]{8}")) errors.add("codigo debe contener 8 caracteres alfanuméricos.");
                else if (!codes.add(code.toUpperCase(Locale.ROOT)) || productos.existsByCodigo(code.toUpperCase(Locale.ROOT))) errors.add("codigo duplicado en archivo o catálogo.");
            }
            BigDecimal purchase = number(get(cells, columns, "precioCompra"));
            BigDecimal selling = number(get(cells, columns, "precioVenta"));
            BigDecimal minimum = number(get(cells, columns, "stockMinimo"));
            BigDecimal qty = number(get(cells, columns, "cantidad"));
            if (purchase.signum() < 0 || selling.signum() < 0 || minimum.signum() < 0 || qty.signum() <= 0) errors.add("precios/stock mínimo deben ser >= 0 y cantidad debe ser > 0.");
            String lotNo = get(cells, columns, "numeroLote").trim();
            if (lotNo.isBlank()) errors.add("numeroLote es obligatorio.");
            LocalDate received = date(get(cells, columns, "fechaIngreso"));
            LocalDate expiry = date(get(cells, columns, "fechaVencimiento"));
            if (expiry.isBefore(received)) errors.add("fechaVencimiento no puede ser anterior a fechaIngreso.");
            ProductoRq product = new ProductoRq(name, optional(cells, columns, "descripcion"), optional(cells, columns, "presentacion"),
                    code.isBlank() ? null : code, type, invima.isBlank() ? null : invima, purchase, selling, minimum,
                    optional(cells, columns, "fechaProximoPedido").isBlank() ? null : date(optional(cells, columns, "fechaProximoPedido")), true);
            EntradaLoteRq lote = new EntradaLoteRq(null, lotNo, received, expiry, qty, purchase, selling,
                    optional(cells, columns, "referenciaProveedor"), null);
            return new ImportacionFila(rowNum, product, lote, errors);
        } catch (Exception e) {
            errors.add("Valor inválido: " + Optional.ofNullable(e.getMessage()).orElse("revise cantidades, fechas y precios."));
            return new ImportacionFila(rowNum, null, null, errors);
        }
    }

    private Map<String, Integer> validateHeaders(List<String> header) {
        Map<String, Integer> map = new HashMap<>();
        for (int i = 0; i < header.size(); i++) {
            String key = header.get(i).trim().toLowerCase(Locale.ROOT);
            if (map.put(key, i) != null) throw new BadRequestException("El archivo tiene encabezados duplicados.");
        }
        List<String> missing = HEADERS.stream().filter(h -> !map.containsKey(h.toLowerCase(Locale.ROOT))).toList();
        if (!missing.isEmpty()) throw new BadRequestException("Faltan encabezados obligatorios: " + String.join(", ", missing));
        return map;
    }
    private static String get(List<String> row, Map<String, Integer> columns, String key) {
        Integer i = columns.get(key.toLowerCase(Locale.ROOT)); return i == null || i >= row.size() ? "" : row.get(i).trim();
    }
    private static String optional(List<String> row, Map<String, Integer> columns, String key) { return get(row, columns, key); }
    private static BigDecimal number(String text) {
        if (text == null || text.isBlank()) throw new IllegalArgumentException("faltan campos numéricos requeridos.");
        return new BigDecimal(text.trim());
    }
    private static LocalDate date(String text) {
        try { return LocalDate.parse(text.trim()); }
        catch (DateTimeParseException e) { throw new IllegalArgumentException("use fechas con formato AAAA-MM-DD."); }
    }
    private List<ImportacionFila> deserialize(String json) {
        try {
            List<?> raw = mapper.readValue(json, List.class);
            return raw.stream().map(r -> mapper.convertValue(r, ImportacionFila.class)).toList();
        } catch (Exception e) { throw new IllegalStateException("No se pudo leer el resultado guardado de la importación.", e); }
    }
    private static List<List<String>> readCsv(MultipartFile file) throws IOException {
        List<List<String>> rows = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line; while ((line = reader.readLine()) != null) {
                if (rows.isEmpty() && line.startsWith("\uFEFF")) line = line.substring(1);
                rows.add(parseCsvLine(line));
            }
        }
        return rows;
    }
    private static List<String> parseCsvLine(String line) {
        List<String> fields = new ArrayList<>(); StringBuilder cell = new StringBuilder(); boolean quoted = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"' && quoted && i + 1 < line.length() && line.charAt(i + 1) == '"') { cell.append('"'); i++; }
            else if (c == '"') quoted = !quoted;
            else if (c == ',' && !quoted) { fields.add(cell.toString()); cell.setLength(0); }
            else cell.append(c);
        }
        if (quoted) throw new IllegalArgumentException("campo CSV con comillas sin cerrar.");
        fields.add(cell.toString()); return fields;
    }
    private static List<List<String>> readXlsx(MultipartFile file) throws Exception {
        List<List<String>> rows = new ArrayList<>(); DataFormatter formatter = new DataFormatter(Locale.ROOT);
        try (InputStream in = file.getInputStream(); Workbook book = WorkbookFactory.create(in)) {
            Sheet sheet = book.getSheetAt(0);
            for (Row row : sheet) {
                List<String> values = new ArrayList<>();
                for (int i = 0; i < row.getLastCellNum(); i++) values.add(formatter.formatCellValue(row.getCell(i)).trim());
                rows.add(values);
            }
        }
        return rows;
    }
}
