package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.LoteMedicamento;
import com.uniminuto.clinica.entity.LogCargueMasivo;
import com.uniminuto.clinica.entity.Medicamento;
import com.uniminuto.clinica.entity.Usuario;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.*;
import com.uniminuto.clinica.repository.LogCargueMasivoRepository;
import com.uniminuto.clinica.repository.MedicamentoRepository;
import com.uniminuto.clinica.repository.UsuarioRepository;
import com.uniminuto.clinica.service.CargaMasivaService;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;

@Service
public class CargaMasivaServiceImpl implements CargaMasivaService {

    private static final List<String> HEADERS = List.of(
            "nombre", "descripcion", "presentacion", "codigoAlfanumerico",
            "precioCompra", "precioVenta", "cantidadActual", "stockMinimo",
            "fechaIngresoInventario", "fechaProximoPedido", "registroInvima",
            "fechaCompra", "fechaVence", "tipoProducto", "numeroLote",
            "cantidadLote", "precioCompraLote", "fechaIngresoLote", "fechaVencimientoLote"
    );

    @Autowired
    private MedicamentoRepository medicamentoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private LogCargueMasivoRepository logCargueMasivoRepository;

    @Override
    public CargaMasivaPreviewRs previsualizarArchivo(MultipartFile archivo) throws BadRequestException {
        if (archivo == null || archivo.isEmpty()) {
            throw new BadRequestException("Debe seleccionar un archivo .xlsx o .csv.");
        }

        String nombre = archivo.getOriginalFilename() == null ? "archivo" : archivo.getOriginalFilename().toLowerCase(Locale.ROOT);
        if (!(nombre.endsWith(".xlsx") || nombre.endsWith(".csv"))) {
            throw new BadRequestException("Solo se permiten archivos con extensión .xlsx o .csv.");
        }

        try {
            List<Map<String, String>> filas = nombre.endsWith(".xlsx")
                    ? leerExcel(archivo)
                    : leerCsv(archivo);

            CargaMasivaPreviewRs preview = new CargaMasivaPreviewRs();
            preview.setNombreArchivo(archivo.getOriginalFilename());
            preview.setTotalRegistros(filas.size());

            Set<String> nombresArchivo = new HashSet<>();
            Set<String> codigosArchivo = new HashSet<>();

            for (int i = 0; i < filas.size(); i++) {
                int numeroFila = i + 2;
                Map<String, String> fila = filas.get(i);
                List<String> errores = validarFila(fila, nombresArchivo, codigosArchivo);

                if (errores.isEmpty()) {
                    MedicamentoRq registro = mapearRegistro(fila);
                    preview.getRegistrosValidosConfirmables().add(registro);
                } else {
                    for (String mensaje : errores) {
                        preview.getErrores().add(new ErrorFilaDto(numeroFila, mensaje));
                    }
                }
            }

            preview.setRegistrosValidos(preview.getRegistrosValidosConfirmables().size());
            preview.setRegistrosConError(preview.getErrores().isEmpty() ? 0
                    : (int) preview.getErrores().stream().map(ErrorFilaDto::getNumeroFila).distinct().count());
            return preview;
        } catch (BadRequestException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BadRequestException("No fue posible procesar el archivo: " + ex.getMessage());
        }
    }

    @Override
    @Transactional
    public MiRespuestaRS confirmarImportacion(ConfirmarCargaMasivaRq solicitud) throws BadRequestException {
        if (solicitud == null || solicitud.getUsuarioResponsableId() == null) {
            throw new BadRequestException("Debe indicar el usuario responsable del cargue.");
        }
        if (solicitud.getRegistros() == null) {
            throw new BadRequestException("La lista de registros a confirmar es obligatoria.");
        }

        Usuario usuario = usuarioRepository.findById(solicitud.getUsuarioResponsableId())
                .orElseThrow(() -> new BadRequestException("El usuario responsable no existe."));

        int exitosos = 0;
        int errores = 0;
        List<String> detalleErrores = new ArrayList<>();

        for (int i = 0; i < solicitud.getRegistros().size(); i++) {
            MedicamentoRq rq = solicitud.getRegistros().get(i);
            try {
                importarRegistroIndividual(rq);
                exitosos++;
            } catch (Exception ex) {
                errores++;
                detalleErrores.add("Fila/registro " + (i + 2) + ": " + ex.getMessage());
            }
        }

        LogCargueMasivo log = new LogCargueMasivo();
        log.setNombreArchivo(solicitud.getNombreArchivo() == null ? "Carga masiva" : solicitud.getNombreArchivo());
        log.setFechaCargue(LocalDateTime.now());
        log.setUsuarioResponsable(usuario);
        log.setTotalRegistrosProcesados(solicitud.getRegistros().size());
        log.setRegistrosExitosos(exitosos);
        log.setRegistrosConError(errores);
        log.setDetalleErrores(detalleErrores.isEmpty() ? null : String.join("\n", detalleErrores));
        logCargueMasivoRepository.save(log);

        MiRespuestaRS respuesta = new MiRespuestaRS();
        respuesta.setStatus(exitosos > 0 ? 200 : 400);
        respuesta.setMessage("Importación procesada: " + exitosos + " exitosos y " + errores + " con error.");
        return respuesta;
    }

    @Override
    public List<LogCargueMasivo> listarHistorialCargues() {
        return logCargueMasivoRepository.findAllByOrderByFechaCargueDesc();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected void importarRegistroIndividual(MedicamentoRq rq) {
        Medicamento medicamento = mapearEntidad(rq);

        if (medicamentoRepository.existsByNombre(medicamento.getNombre().trim())) {
            throw new BadRequestException("Ya existe un medicamento con el nombre " + medicamento.getNombre());
        }
        if (medicamentoRepository.existsByCodigoAlfanumerico(medicamento.getCodigoAlfanumerico().trim())) {
            throw new BadRequestException("Ya existe un medicamento con el código " + medicamento.getCodigoAlfanumerico());
        }

        for (LoteMedicamento lote : medicamento.getLotes()) {
            lote.setMedicamento(medicamento);
        }
        medicamentoRepository.saveAndFlush(medicamento);
    }

    private List<Map<String, String>> leerExcel(MultipartFile archivo) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(archivo.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            validarEncabezados(getRowValues(sheet.getRow(0)));

            List<Map<String, String>> filas = new ArrayList<>();
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                Map<String, String> fila = new LinkedHashMap<>();
                for (int c = 0; c < HEADERS.size(); c++) {
                    fila.put(HEADERS.get(c), getCellValue(row.getCell(c)));
                }
                if (fila.values().stream().allMatch(v -> v == null || v.isBlank())) continue;
                filas.add(fila);
            }
            return filas;
        }
    }

    private List<Map<String, String>> leerCsv(MultipartFile archivo) throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).setTrim(true).build();
        try (CSVParser parser = new CSVParser(new InputStreamReader(archivo.getInputStream(), StandardCharsets.UTF_8), format)) {
            validarEncabezados(parser.getHeaderNames());
            List<Map<String, String>> filas = new ArrayList<>();
            for (CSVRecord record : parser) {
                Map<String, String> fila = new LinkedHashMap<>();
                for (String header : HEADERS) {
                    fila.put(header, record.isMapped(header) ? record.get(header) : "");
                }
                if (fila.values().stream().allMatch(v -> v == null || v.isBlank())) continue;
                filas.add(fila);
            }
            return filas;
        }
    }

    private void validarEncabezados(List<String> encontrados) {
        if (encontrados == null || !new HashSet<>(encontrados).containsAll(HEADERS)) {
            throw new BadRequestException("Los encabezados no coinciden con la plantilla esperada. Encabezados requeridos: " + String.join(", ", HEADERS));
        }
    }

    private List<String> getRowValues(Row row) {
        if (row == null) return Collections.emptyList();
        List<String> values = new ArrayList<>();
        for (int i = 0; i < HEADERS.size(); i++) {
            values.add(getCellValue(row.getCell(i)).trim());
        }
        return values;
    }

    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> DateUtil.isCellDateFormatted(cell)
                    ? cell.getLocalDateTimeCellValue().toLocalDate().toString()
                    : String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN -> Boolean.toString(cell.getBooleanCellValue());
            default -> "";
        };
    }

    private List<String> validarFila(Map<String, String> fila, Set<String> nombresArchivo, Set<String> codigosArchivo) {
        List<String> errores = new ArrayList<>();
        String nombre = value(fila, "nombre");
        String codigo = value(fila, "codigoAlfanumerico");
        String tipo = value(fila, "tipoProducto").toLowerCase(Locale.ROOT);

        if (nombre.isBlank()) errores.add("El nombre es obligatorio.");
        else {
            String clave = nombre.toLowerCase(Locale.ROOT);
            if (nombresArchivo.contains(clave)) errores.add("El nombre está repetido dentro del archivo.");
            if (medicamentoRepository.existsByNombre(nombre)) errores.add("El nombre ya existe en la base de datos.");
            nombresArchivo.add(clave);
        }

        if (!codigo.matches("[A-Za-z0-9]{8}")) errores.add("El código alfanumérico debe tener exactamente 8 caracteres alfanuméricos.");
        else {
            if (codigosArchivo.contains(codigo.toUpperCase(Locale.ROOT))) errores.add("El código alfanumérico está repetido dentro del archivo.");
            if (medicamentoRepository.existsByCodigoAlfanumerico(codigo)) errores.add("El código alfanumérico ya existe en la base de datos.");
            codigosArchivo.add(codigo.toUpperCase(Locale.ROOT));
        }

        if (tipo.equals("medicamento") && value(fila, "registroInvima").isBlank()) {
            errores.add("El registro INVIMA es obligatorio para medicamentos.");
        }

        Double precioCompra = parsePositiveDouble(fila, "precioCompra", errores, "precio de compra");
        Double precioVenta = parsePositiveDouble(fila, "precioVenta", errores, "precio de venta");
        if (precioCompra != null && precioVenta != null && precioVenta <= 0) errores.add("El precio de venta debe ser positivo.");

        Integer cantidad = parsePositiveInt(fila, "cantidadActual", errores, "cantidad actual", true);
        Integer stock = parsePositiveInt(fila, "stockMinimo", errores, "stock mínimo", false);

        LocalDate ingreso = parseDate(fila, "fechaIngresoInventario", errores, "fecha de ingreso al inventario", true);
        LocalDate compra = parseDate(fila, "fechaCompra", errores, "fecha de compra", true);
        LocalDate vence = parseDate(fila, "fechaVence", errores, "fecha de vencimiento", true);
        parseOptionalDate(fila, "fechaProximoPedido", errores, "fecha del próximo pedido");

        if (compra != null && vence != null && vence.isBefore(compra)) {
            errores.add("La fecha de vencimiento no puede ser anterior a la fecha de compra.");
        }

        if (!value(fila, "numeroLote").isBlank()) {
            if (value(fila, "cantidadLote").isBlank()) errores.add("La cantidad del lote es obligatoria.");
            parsePositiveInt(fila, "cantidadLote", errores, "cantidad del lote", true);
            parsePositiveDouble(fila, "precioCompraLote", errores, "precio de compra del lote");
            LocalDate ingresoLote = parseDate(fila, "fechaIngresoLote", errores, "fecha de ingreso del lote", true);
            LocalDate venceLote = parseDate(fila, "fechaVencimientoLote", errores, "fecha de vencimiento del lote", true);
            if (ingresoLote != null && venceLote != null && venceLote.isBefore(ingresoLote)) {
                errores.add("La fecha de vencimiento del lote no puede ser anterior a su fecha de ingreso.");
            }
        }

        if (cantidad == null || stock == null) {
            // Los errores ya fueron agregados por los métodos de parseo.
        }
        return errores;
    }

    private MedicamentoRq mapearRegistro(Map<String, String> fila) {
        MedicamentoRq rq = new MedicamentoRq();
        rq.setNombre(value(fila, "nombre"));
        rq.setDescripcion(value(fila, "descripcion"));
        rq.setPresentacion(value(fila, "presentacion"));
        rq.setCodigoAlfanumerico(value(fila, "codigoAlfanumerico"));
        rq.setPrecioCompra(parsePlainDouble(value(fila, "precioCompra")));
        rq.setPrecioVenta(parsePlainDouble(value(fila, "precioVenta")));
        rq.setCantidadActual(parsePlainInt(value(fila, "cantidadActual")));
        rq.setStockMinimo(parsePlainInt(value(fila, "stockMinimo")));
        rq.setFechaIngresoInventario(parsePlainDate(value(fila, "fechaIngresoInventario")));
        rq.setFechaProximoPedido(parsePlainOptionalDate(value(fila, "fechaProximoPedido")));
        rq.setRegistroInvima(value(fila, "registroInvima").isBlank() ? null : value(fila, "registroInvima"));
        rq.setFechaCompra(parsePlainDate(value(fila, "fechaCompra")));
        rq.setFechaVence(parsePlainDate(value(fila, "fechaVence")));

        if (!value(fila, "numeroLote").isBlank()) {
            LoteMedicamentoRq lote = new LoteMedicamentoRq();
            lote.setNumeroLote(value(fila, "numeroLote"));
            lote.setCantidad(parsePlainInt(value(fila, "cantidadLote")));
            lote.setPrecioCompraLote(parsePlainDouble(value(fila, "precioCompraLote")));
            lote.setFechaIngresoLote(parsePlainDate(value(fila, "fechaIngresoLote")));
            lote.setFechaVencimientoLote(parsePlainDate(value(fila, "fechaVencimientoLote")));
            rq.setLotes(List.of(lote));
        } else {
            rq.setLotes(Collections.emptyList());
        }
        return rq;
    }

    private Medicamento mapearEntidad(MedicamentoRq rq) {
        Medicamento m = new Medicamento();
        m.setNombre(rq.getNombre().trim());
        m.setDescripcion(rq.getDescripcion());
        m.setPresentacion(rq.getPresentacion());
        m.setCodigoAlfanumerico(rq.getCodigoAlfanumerico().trim());
        m.setPrecioCompra(rq.getPrecioCompra());
        m.setPrecioVenta(rq.getPrecioVenta());
        m.setCantidadActual(rq.getCantidadActual());
        m.setStockMinimo(rq.getStockMinimo());
        m.setFechaIngresoInventario(rq.getFechaIngresoInventario());
        m.setFechaProximoPedido(rq.getFechaProximoPedido());
        m.setRegistroInvima(rq.getRegistroInvima());
        m.setFechaCompra(rq.getFechaCompra());
        m.setFechaVence(rq.getFechaVence());

        if (rq.getLotes() != null) {
            for (LoteMedicamentoRq loteRq : rq.getLotes()) {
                LoteMedicamento lote = new LoteMedicamento();
                lote.setNumeroLote(loteRq.getNumeroLote());
                lote.setCantidad(loteRq.getCantidad());
                lote.setPrecioCompraLote(loteRq.getPrecioCompraLote());
                lote.setFechaIngresoLote(loteRq.getFechaIngresoLote());
                lote.setFechaVencimientoLote(loteRq.getFechaVencimientoLote());
                lote.setMedicamento(m);
                m.getLotes().add(lote);
            }
        }
        if (!m.getLotes().isEmpty()) {
            m.setCantidadActual(m.getLotes().stream().mapToInt(LoteMedicamento::getCantidad).sum());
        }
        return m;
    }

    private String value(Map<String, String> fila, String key) {
        String value = fila.get(key);
        return value == null ? "" : value.trim();
    }

    private Double parsePositiveDouble(Map<String, String> fila, String key, List<String> errores, String campo) {
        String texto = value(fila, key);
        if (texto.isBlank()) {
            errores.add("El " + campo + " es obligatorio.");
            return null;
        }
        try {
            double valor = Double.parseDouble(texto);
            if (valor <= 0) errores.add("El " + campo + " debe ser positivo.");
            return valor;
        } catch (NumberFormatException ex) {
            errores.add("El " + campo + " no tiene un formato numérico válido.");
            return null;
        }
    }

    private Integer parsePositiveInt(Map<String, String> fila, String key, List<String> errores, String campo, boolean positivoEstricto) {
        String texto = value(fila, key);
        if (texto.isBlank()) {
            if (positivoEstricto) errores.add("La " + campo + " es obligatoria.");
            return null;
        }
        try {
            int valor = Integer.parseInt(texto);
            if (positivoEstricto ? valor <= 0 : valor < 0) errores.add("La " + campo + " debe ser " + (positivoEstricto ? "positiva." : "cero o positiva."));
            return valor;
        } catch (NumberFormatException ex) {
            errores.add("La " + campo + " no tiene un formato entero válido.");
            return null;
        }
    }

    private LocalDate parseDate(Map<String, String> fila, String key, List<String> errores, String campo, boolean obligatoria) {
        String texto = value(fila, key);
        if (texto.isBlank()) {
            if (obligatoria) errores.add("La " + campo + " es obligatoria.");
            return null;
        }
        try {
            return LocalDate.parse(texto);
        } catch (DateTimeParseException ex) {
            errores.add("La " + campo + " no tiene formato ISO válido (AAAA-MM-DD).");
            return null;
        }
    }

    private void parseOptionalDate(Map<String, String> fila, String key, List<String> errores, String campo) {
        if (value(fila, key).isBlank()) return;
        parseDate(fila, key, errores, campo, false);
    }

    private Double parsePlainDouble(String value) {
        return value == null || value.isBlank() ? null : Double.parseDouble(value);
    }

    private Integer parsePlainInt(String value) {
        return value == null || value.isBlank() ? null : Integer.parseInt(value);
    }

    private LocalDate parsePlainDate(String value) {
        return value == null || value.isBlank() ? null : LocalDate.parse(value);
    }

    private LocalDate parsePlainOptionalDate(String value) {
        return value == null || value.isBlank() ? null : LocalDate.parse(value);
    }
}
