package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.ImportacionInventario;
import com.uniminuto.clinica.entity.ImportacionInventarioDetalle;
import com.uniminuto.clinica.entity.Usuario;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.*;
import com.uniminuto.clinica.repository.*;
import com.uniminuto.clinica.service.InventarioService;
import org.apache.poi.ss.usermodel.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class ImportacionInventarioService {
    private static final int MAX_BYTES = 10 * 1024 * 1024;
    private static final int MAX_FILAS = 5000;
    private final MedicamentoRepository medicamentoRepository;
    private final LoteMedicamentoRepository loteRepository;
    private final UsuarioRepository usuarioRepository;
    private final ImportacionInventarioRepository importacionRepository;
    private final ImportacionInventarioDetalleRepository detalleRepository;
    private final InventarioService inventarioService;

    public ImportacionInventarioService(MedicamentoRepository medicamentoRepository,
            LoteMedicamentoRepository loteRepository, UsuarioRepository usuarioRepository,
            ImportacionInventarioRepository importacionRepository,
            ImportacionInventarioDetalleRepository detalleRepository, InventarioService inventarioService) {
        this.medicamentoRepository = medicamentoRepository; this.loteRepository = loteRepository;
        this.usuarioRepository = usuarioRepository; this.importacionRepository = importacionRepository;
        this.detalleRepository = detalleRepository; this.inventarioService = inventarioService;
    }

    public ImportacionInventarioRs previsualizar(MultipartFile file) {
        List<Row> rows = parse(file);
        List<FilaImportacionInventarioRs> results = validar(rows);
        ImportacionInventarioRs response = resumen(rows.size(), results);
        return response;
    }

    public ImportacionInventarioRs ejecutar(MultipartFile file) {
        List<Row> rows = parse(file);
        List<FilaImportacionInventarioRs> results = validar(rows);
        Usuario usuario = usuarioActual();
        int imported = 0;
        for (int i = 0; i < rows.size(); i++) {
            FilaImportacionInventarioRs result = results.get(i);
            if (!"VALIDA".equals(result.getEstado())) continue;
            Row row = rows.get(i);
            try {
                Long medicamentoId = medicamentoRepository.findByCodigoIgnoreCase(row.codigoProducto).orElseThrow().getId();
                IngresoLoteRq ingreso = new IngresoLoteRq();
                ingreso.setMedicamentoId(medicamentoId); ingreso.setCodigoLote(row.codigoLote);
                ingreso.setFechaVencimiento(row.vencimiento); ingreso.setCantidad(row.cantidad);
                ingreso.setCostoUnitario(row.costo);
                inventarioService.registrarIngreso(ingreso);
                result.setEstado("IMPORTADA"); imported++;
            } catch (RuntimeException ex) {
                result.setEstado("RECHAZADA");
                result.setMensaje(mensajeSeguro(ex));
            }
        }
        ImportacionInventario audit = new ImportacionInventario();
        audit.setArchivo(nombreSeguro(file.getOriginalFilename()));
        audit.setSha256(hash(file)); audit.setUsuario(usuario); audit.setTotalFilas(rows.size());
        audit.setFilasImportadas(imported); audit.setFilasRechazadas(rows.size() - imported);
        audit.setFecha(LocalDateTime.now());
        final ImportacionInventario savedAudit = importacionRepository.save(audit);
        List<ImportacionInventarioDetalle> details = results.stream().map(result -> {
            ImportacionInventarioDetalle detail = new ImportacionInventarioDetalle();
            detail.setImportacion(savedAudit); detail.setNumeroFila(result.getFila());
            detail.setCodigoProducto(result.getCodigoProducto()); detail.setCodigoLote(result.getCodigoLote());
            detail.setEstado(result.getEstado()); detail.setMensaje(result.getMensaje()); return detail;
        }).toList();
        detalleRepository.saveAll(details);
        ImportacionInventarioRs response = resumen(rows.size(), results);
        response.setImportacionId(savedAudit.getId()); response.setFilasImportadas(imported);
        response.setFilasRechazadas(rows.size() - imported);
        response.setFilasValidas(imported);
        return response;
    }

    private List<FilaImportacionInventarioRs> validar(List<Row> rows) {
        Set<String> lotesArchivo = new HashSet<>();
        List<FilaImportacionInventarioRs> results = new ArrayList<>();
        for (Row row : rows) {
            FilaImportacionInventarioRs out = new FilaImportacionInventarioRs();
            out.setFila(row.numero); out.setCodigoProducto(row.codigoProducto); out.setCodigoLote(row.codigoLote);
            String error = row.error;
            if (error == null && medicamentoRepository.findByCodigoIgnoreCase(row.codigoProducto).isEmpty()) error = "No existe el código de producto.";
            if (error == null && loteRepository.existsByMedicamento_IdAndCodigoLoteIgnoreCase(
                    medicamentoRepository.findByCodigoIgnoreCase(row.codigoProducto).orElseThrow().getId(), row.codigoLote)) error = "El código de lote ya está registrado.";
            String llave = row.codigoProducto + "|" + row.codigoLote.toLowerCase(Locale.ROOT);
            if (error == null && !lotesArchivo.add(llave)) error = "Código de lote repetido dentro del archivo.";
            out.setEstado(error == null ? "VALIDA" : "RECHAZADA"); out.setMensaje(error);
            results.add(out);
        }
        return results;
    }

    private List<Row> parse(MultipartFile file) {
        if (file == null || file.isEmpty()) throw new BadRequestException("Adjunta un archivo CSV o XLSX.");
        if (file.getSize() > MAX_BYTES) throw new BadRequestException("El archivo supera el límite de 10 MB.");
        String name = nombreSeguro(file.getOriginalFilename()).toLowerCase(Locale.ROOT);
        try {
            List<List<String>> values = name.endsWith(".xlsx") ? parseXlsx(file) : name.endsWith(".csv") ? parseCsv(file) : null;
            if (values == null) throw new BadRequestException("El formato debe ser .csv o .xlsx.");
            if (values.isEmpty()) throw new BadRequestException("El archivo no contiene filas.");
            Map<String, Integer> columns = new HashMap<>();
            for (int i = 0; i < values.get(0).size(); i++) columns.put(normalizar(values.get(0).get(i)), i);
            for (String required : List.of("codigoproducto", "codigolote", "fechavencimiento", "cantidad", "costounitario")) {
                if (!columns.containsKey(required)) throw new BadRequestException("Falta columna requerida: " + required + ".");
            }
            if (values.size() - 1 > MAX_FILAS) throw new BadRequestException("El máximo por archivo es 5.000 filas.");
            List<Row> result = new ArrayList<>();
            for (int i = 1; i < values.size(); i++) {
                List<String> cells = values.get(i);
                if (cells.stream().allMatch(String::isBlank)) continue;
                Row row = new Row(); row.numero = i + 1;
                row.codigoProducto = cell(cells, columns.get("codigoproducto")).toUpperCase(Locale.ROOT);
                row.codigoLote = cell(cells, columns.get("codigolote"));
                try {
                    row.vencimiento = LocalDate.parse(cell(cells, columns.get("fechavencimiento")));
                    row.cantidad = Integer.valueOf(cell(cells, columns.get("cantidad")));
                    row.costo = new BigDecimal(cell(cells, columns.get("costounitario")))
                            .setScale(2, java.math.RoundingMode.HALF_UP);
                    if (!row.codigoProducto.matches("[A-Z0-9]{8}")) throw new IllegalArgumentException("Código de producto inválido.");
                    if (row.codigoLote.isBlank() || row.codigoLote.length() > 80) throw new IllegalArgumentException("Código de lote inválido.");
                    if (!row.vencimiento.isAfter(LocalDate.now())) throw new IllegalArgumentException("Fecha de vencimiento debe ser futura.");
                    if (row.cantidad <= 0 || row.costo.signum() < 0) throw new IllegalArgumentException("Cantidad debe ser positiva y costo no negativo.");
                } catch (Exception ex) { row.error = ex instanceof IllegalArgumentException && ex.getMessage() != null ? ex.getMessage() : "Datos inválidos; revisa fecha, cantidad y costo."; }
                result.add(row);
            }
            return result;
        } catch (BadRequestException ex) { throw ex; }
        catch (Exception ex) { throw new BadRequestException("No se pudo leer el archivo. Verifica plantilla y contenido."); }
    }

    private List<List<String>> parseCsv(MultipartFile file) throws Exception {
        String content = new String(file.getBytes(), StandardCharsets.UTF_8).replace("\uFEFF", "");
        List<List<String>> rows = new ArrayList<>();
        for (String line : content.split("\\R")) {
            if (line.isBlank()) continue;
            String delimiter = line.contains(";") ? ";" : ",";
            rows.add(Arrays.stream(line.split(java.util.regex.Pattern.quote(delimiter), -1))
                    .map(String::trim).map(v -> v.replaceAll("^\"|\"$", "")).toList());
        }
        return rows;
    }

    private List<List<String>> parseXlsx(MultipartFile file) throws Exception {
        List<List<String>> rows = new ArrayList<>();
        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(file.getBytes()))) {
            Sheet sheet = workbook.getSheetAt(0); DataFormatter formatter = new DataFormatter(Locale.ROOT);
            for (org.apache.poi.ss.usermodel.Row row : sheet) {
                List<String> cells = new ArrayList<>();
                for (int c = 0; c < Math.max(row.getLastCellNum(), 0); c++) cells.add(formatter.formatCellValue(row.getCell(c)).trim());
                rows.add(cells);
            }
        }
        return rows;
    }

    private String cell(List<String> values, int index) { return index < values.size() ? values.get(index).trim() : ""; }
    private String normalizar(String s) { return s == null ? "" : s.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z]", ""); }
    private Usuario usuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) throw new BadRequestException("Se requiere una sesión autenticada.");
        return usuarioRepository.findByUsername(auth.getName()).orElseThrow(() -> new BadRequestException("Usuario autenticado no encontrado."));
    }
    private String nombreSeguro(String value) { return value == null ? "archivo" : value.replaceAll("[^A-Za-z0-9._-]", "_"); }
    private String hash(MultipartFile file) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(file.getBytes())); }
        catch (Exception ex) { throw new BadRequestException("No se pudo calcular la huella del archivo."); }
    }
    private String mensajeSeguro(RuntimeException ex) { return ex.getMessage() == null ? "Error al importar fila." : ex.getMessage().substring(0, Math.min(500, ex.getMessage().length())); }
    private ImportacionInventarioRs resumen(int total, List<FilaImportacionInventarioRs> results) {
        ImportacionInventarioRs response = new ImportacionInventarioRs(); response.setTotalFilas(total);
        response.setFilasValidas((int) results.stream().filter(r -> "VALIDA".equals(r.getEstado())).count());
        response.setFilas(results); return response;
    }
    private static class Row { int numero; String codigoProducto; String codigoLote; LocalDate vencimiento; Integer cantidad; BigDecimal costo; String error; }
}
