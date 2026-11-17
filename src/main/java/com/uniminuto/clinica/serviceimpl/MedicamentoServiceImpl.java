package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.InventarioLote;
import com.uniminuto.clinica.entity.Medicamento;
import com.uniminuto.clinica.entity.MovimientoInventario;
import com.uniminuto.clinica.entity.Usuario;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.MedicamentoRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.repository.InventarioLoteRepository;
import com.uniminuto.clinica.repository.MedicamentoRepository;
import com.uniminuto.clinica.repository.MovimientoInventarioRepository;
import com.uniminuto.clinica.repository.UsuarioRepository;
import com.uniminuto.clinica.service.MedicamentoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/** Implementa administración y consultas del inventario de productos médicos. */
@Service
public class MedicamentoServiceImpl implements MedicamentoService {

    private static final Pattern CODIGO_PRODUCTO = Pattern.compile("^[A-Za-z0-9]{8}$");
    private final MedicamentoRepository medicamentoRepository;
    private final UsuarioRepository usuarioRepository;
    private final InventarioLoteRepository inventarioLoteRepository;
    private final MovimientoInventarioRepository movimientoInventarioRepository;

    /** Construye el servicio con sus repositorios de inventario y usuarios. */
    public MedicamentoServiceImpl(MedicamentoRepository medicamentoRepository,
                                  UsuarioRepository usuarioRepository,
                                  InventarioLoteRepository inventarioLoteRepository,
                                  MovimientoInventarioRepository movimientoInventarioRepository) {
        this.medicamentoRepository = medicamentoRepository;
        this.usuarioRepository = usuarioRepository;
        this.inventarioLoteRepository = inventarioLoteRepository;
        this.movimientoInventarioRepository = movimientoInventarioRepository;
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public List<Medicamento> listarMedicamentos() {
        validarConsultaMedicamentos();
        return medicamentoRepository.findAllByOrderByNombreAsc();
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public List<Medicamento> buscarMedicamentos(String termino) {
        validarConsultaMedicamentos();
        String busqueda = normalizarBusqueda(termino);
        return listarMedicamentos().stream()
                .filter(medicamento -> busqueda.isEmpty()
                        || contiene(medicamento.getNombre(), busqueda)
                        || contiene(medicamento.getCodigo(), busqueda)
                        || contiene(medicamento.getDescripcion(), busqueda)
                        || contiene(medicamento.getPresentacion(), busqueda))
                .toList();
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public List<Medicamento> listarDisponibles() {
        validarConsultaMedicamentos();
        return medicamentoRepository.findAllByOrderByNombreAsc().stream()
                .filter(medicamento -> cantidadDisponible(medicamento) > 0)
                .toList();
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public List<Medicamento> listarAgotados() {
        validarConsultaMedicamentos();
        return medicamentoRepository.findAllByOrderByNombreAsc().stream()
                .filter(medicamento -> cantidadDisponible(medicamento) == 0)
                .toList();
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public List<Medicamento> listarStockBajo() {
        validarConsultaMedicamentos();
        return medicamentoRepository.buscarMedicamentosConStockBajo();
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public List<Medicamento> listarProximosAVencer(int dias) {
        validarConsultaMedicamentos();
        if (dias < 0 || dias > 3650) {
            throw new BadRequestException("El rango de vencimiento debe estar entre 0 y 3650 días.");
        }
        Map<Long, Medicamento> medicamentos = new LinkedHashMap<>();
        inventarioLoteRepository.buscarLotesPorVencer(LocalDate.now(), LocalDate.now().plusDays(dias))
                .forEach(lote -> medicamentos.put(lote.getMedicamento().getId(), lote.getMedicamento()));
        return List.copyOf(medicamentos.values());
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> consultarDisponibilidad(Long medicamentoId, Integer cantidadRequerida) {
        validarConsultaMedicamentos();
        validarId(medicamentoId);
        if (cantidadRequerida == null || cantidadRequerida < 0) {
            throw new BadRequestException("La cantidad requerida no puede ser negativa.");
        }
        Medicamento medicamento = medicamentoRepository.findById(medicamentoId)
                .orElseThrow(() -> new BadRequestException("El medicamento no existe."));
        int existencia = cantidadDisponible(medicamento);
        Map<String, Object> resultado = new LinkedHashMap<>();
        resultado.put("medicamentoId", medicamentoId);
        resultado.put("nombre", medicamento.getNombre());
        resultado.put("cantidadDisponible", existencia);
        resultado.put("cantidadRequerida", cantidadRequerida);
        resultado.put("suficiente", existencia >= cantidadRequerida);
        return resultado;
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public byte[] exportarInventarioExcel(String tipo, LocalDate desde, LocalDate hasta) {
        validarAccesoInformeInventario();
        validarRangoFechas(desde, hasta);
        String filtro = tipo == null || tipo.isBlank() ? "TODOS" : tipo.trim().toUpperCase(Locale.ROOT);
        if (!List.of("TODOS", "BAJO", "AGOTADO", "PROXIMO_VENCER", "VENCIDO", "ENTRADAS", "SALIDAS")
                .contains(filtro)) {
            throw new BadRequestException("El tipo de informe debe ser TODOS, BAJO, AGOTADO, PROXIMO_VENCER, VENCIDO, ENTRADAS o SALIDAS.");
        }

        try (Workbook libro = new XSSFWorkbook(); ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            CellStyle encabezado = estiloEncabezado(libro);
            List<InventarioLote> lotes = inventarioLoteRepository.findAllByOrderByFechaVencimientoAsc();
            Map<Long, List<InventarioLote>> lotesPorMedicamento = lotes.stream()
                    .filter(lote -> lote.getMedicamento() != null && lote.getMedicamento().getId() != null)
                    .collect(java.util.stream.Collectors.groupingBy(lote -> lote.getMedicamento().getId()));
            crearHojaParametros(libro, encabezado, filtro, desde, hasta);
            crearHojaExistencias(libro, encabezado, filtro, lotesPorMedicamento);
            crearHojaLotes(libro, encabezado, filtro, lotes);
            crearHojaMovimientos(libro, encabezado, filtro, desde, hasta);
            libro.write(salida);
            return salida.toByteArray();
        } catch (IOException exception) {
            throw new BadRequestException("No fue posible generar el informe Excel del inventario.");
        }
    }

    /** Crea la hoja que documenta los filtros aplicados al archivo. */
    private void crearHojaParametros(Workbook libro, CellStyle encabezado, String filtro,
                                     LocalDate desde, LocalDate hasta) {
        Sheet hoja = libro.createSheet("Parámetros");
        escribirEncabezados(hoja, encabezado, "Parámetro", "Valor");
        escribirFila(hoja, 1, "Generado", LocalDateTime.now().toString());
        escribirFila(hoja, 2, "Tipo de informe", filtro);
        escribirFila(hoja, 3, "Desde", desde == null ? "Sin límite" : desde.toString());
        escribirFila(hoja, 4, "Hasta", hasta == null ? "Sin límite" : hasta.toString());
        escribirFila(hoja, 5, "Moneda", "COP");
        hoja.setColumnWidth(0, 6500);
        hoja.setColumnWidth(1, 7000);
    }

    /** Crea el resumen por producto con existencias, precios y valoración. */
    /** Construye el resumen valorizado de existencias y aplica el filtro seleccionado. */
    private void crearHojaExistencias(Workbook libro, CellStyle encabezado, String filtro,
                                      Map<Long, List<InventarioLote>> lotesPorMedicamento) {
        Sheet hoja = libro.createSheet("Existencias");
        String[] columnas = {"ID", "Código", "Medicamento / producto", "Tipo", "Presentación", "Registro Invima",
                "Existencia actual", "Stock mínimo", "Costo unitario (COP)", "Precio venta (COP)",
                "Valor inventario costo (COP)", "Valor inventario venta (COP)", "Estado"};
        escribirEncabezados(hoja, encabezado, columnas);
        int fila = 1;
        LocalDate hoy = LocalDate.now();
        for (Medicamento medicamento : medicamentoRepository.findAllByOrderByNombreAsc()) {
            int stock = medicamento.getCantidadActual() == null ? 0 : medicamento.getCantidadActual();
            int minimo = medicamento.getStockMinimo() == null ? 0 : medicamento.getStockMinimo();
            boolean bajo = stock > 0 && stock <= minimo;
            boolean agotado = stock == 0;
            List<InventarioLote> lotes = lotesPorMedicamento.getOrDefault(medicamento.getId(), List.of());
            boolean vencido = tieneLoteConVencimiento(lotes, hoy, true);
            boolean proximo = tieneLoteProximoAVencer(lotes, hoy);
            if (!coincideFiltroExistencia(filtro, bajo, agotado, proximo, vencido)) {
                continue;
            }
            Row row = hoja.createRow(fila++);
            int col = 0;
            poner(row, col++, medicamento.getId());
            poner(row, col++, medicamento.getCodigo());
            poner(row, col++, medicamento.getNombre());
            poner(row, col++, medicamento.getTipoProducto());
            poner(row, col++, medicamento.getPresentacion());
            poner(row, col++, medicamento.getRegistroInvima());
            poner(row, col++, stock);
            poner(row, col++, minimo);
            poner(row, col++, medicamento.getPrecioCompra());
            poner(row, col++, medicamento.getPrecioVenta());
            poner(row, col++, multiplicar(medicamento.getPrecioCompra(), stock));
            poner(row, col++, multiplicar(medicamento.getPrecioVenta(), stock));
            poner(row, col, estadoInventario(stock, minimo, vencido, proximo));
        }
        finalizarHoja(hoja, columnas.length);
        formatearMoneda(libro, hoja, 8, 9, 10, 11);
    }

    /** Construye el detalle de lotes con fechas, cantidades y estado de vencimiento. */
    private void crearHojaLotes(Workbook libro, CellStyle encabezado, String filtro, List<InventarioLote> lotes) {
        Sheet hoja = libro.createSheet("Lotes");
        String[] columnas = {"ID lote", "ID medicamento", "Medicamento", "Número de lote", "Cantidad",
                "Costo unitario (COP)", "Valor del lote (COP)", "Fecha ingreso", "Fecha vencimiento", "Activo", "Estado"};
        escribirEncabezados(hoja, encabezado, columnas);
        LocalDate hoy = LocalDate.now();
        int fila = 1;
        for (InventarioLote lote : lotes) {
            boolean expirado = lote.getFechaVencimiento() != null && lote.getFechaVencimiento().isBefore(hoy);
            boolean proximo = lote.getFechaVencimiento() != null && !expirado
                    && !lote.getFechaVencimiento().isAfter(hoy.plusDays(30));
            if ("VENCIDO".equals(filtro) && !expirado || "PROXIMO_VENCER".equals(filtro) && !proximo) {
                continue;
            }
            Row row = hoja.createRow(fila++);
            int col = 0;
            poner(row, col++, lote.getId());
            poner(row, col++, lote.getMedicamento() == null ? null : lote.getMedicamento().getId());
            poner(row, col++, lote.getMedicamento() == null ? null : lote.getMedicamento().getNombre());
            poner(row, col++, lote.getNumeroLote());
            int cantidad = lote.getCantidad() == null ? 0 : lote.getCantidad();
            poner(row, col++, cantidad);
            poner(row, col++, lote.getPrecioCompraLote());
            poner(row, col++, multiplicar(lote.getPrecioCompraLote(), cantidad));
            poner(row, col++, lote.getFechaIngreso());
            poner(row, col++, lote.getFechaVencimiento());
            poner(row, col++, Boolean.TRUE.equals(lote.getActivo()) ? "Sí" : "No");
            poner(row, col, expirado ? "VENCIDO" : proximo ? "PRÓXIMO A VENCER" : "VIGENTE");
        }
        finalizarHoja(hoja, columnas.length);
        formatearMoneda(libro, hoja, 5, 6);
    }

    /** Construye el historial de movimientos y aplica el rango de fechas solicitado. */
    private void crearHojaMovimientos(Workbook libro, CellStyle encabezado, String filtro,
                                       LocalDate desde, LocalDate hasta) {
        Sheet hoja = libro.createSheet("Movimientos");
        String[] columnas = {"ID movimiento", "Fecha", "Tipo", "ID medicamento", "Medicamento", "ID lote",
                "Lote", "Cantidad", "Usuario responsable", "Fórmula", "Despacho", "Observación"};
        escribirEncabezados(hoja, encabezado, columnas);
        int fila = 1;
        for (MovimientoInventario movimiento : movimientoInventarioRepository
                .findAllByOrderByFechaMovimientoDescIdDesc()) {
            LocalDate fecha = movimiento.getFechaMovimiento() == null ? null
                    : movimiento.getFechaMovimiento().toLocalDate();
            if (fecha != null && (desde != null && fecha.isBefore(desde) || hasta != null && fecha.isAfter(hasta))) {
                continue;
            }
            String tipoMovimiento = movimiento.getTipo() == null ? "" : movimiento.getTipo().toUpperCase(Locale.ROOT);
            if ("ENTRADAS".equals(filtro) && !tipoMovimiento.equals("ENTRADA")
                    || "SALIDAS".equals(filtro) && !tipoMovimiento.equals("SALIDA")) {
                continue;
            }
            Row row = hoja.createRow(fila++);
            int col = 0;
            poner(row, col++, movimiento.getId());
            poner(row, col++, movimiento.getFechaMovimiento());
            poner(row, col++, movimiento.getTipo());
            poner(row, col++, movimiento.getMedicamento() == null ? null : movimiento.getMedicamento().getId());
            poner(row, col++, movimiento.getMedicamento() == null ? null : movimiento.getMedicamento().getNombre());
            poner(row, col++, movimiento.getLote() == null ? null : movimiento.getLote().getId());
            poner(row, col++, movimiento.getLote() == null ? null : movimiento.getLote().getNumeroLote());
            poner(row, col++, movimiento.getCantidad());
            poner(row, col++, movimiento.getUsuario() == null ? null : movimiento.getUsuario().getUsername());
            poner(row, col++, movimiento.getFormula() == null ? null : movimiento.getFormula().getId());
            poner(row, col++, movimiento.getDespacho() == null ? null : movimiento.getDespacho().getNumeroDespacho());
            poner(row, col, movimiento.getObservacion());
        }
        finalizarHoja(hoja, columnas.length);
    }

    /** Aplica formato visual uniforme a las filas de encabezado. */
    private CellStyle estiloEncabezado(Workbook libro) {
        CellStyle estilo = libro.createCellStyle();
        estilo.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        estilo.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        estilo.setBorderBottom(BorderStyle.THIN);
        org.apache.poi.ss.usermodel.Font fuente = libro.createFont();
        fuente.setBold(true);
        fuente.setColor(IndexedColors.WHITE.getIndex());
        estilo.setFont(fuente);
        return estilo;
    }

    /** Escribe los títulos de columna con el estilo del libro. */
    private void escribirEncabezados(Sheet hoja, CellStyle estilo, String... nombres) {
        Row fila = hoja.createRow(0);
        for (int i = 0; i < nombres.length; i++) {
            Cell celda = fila.createCell(i);
            celda.setCellValue(nombres[i]);
            celda.setCellStyle(estilo);
        }
    }

    /** Escribe un par parámetro/valor en la hoja de filtros. */
    private void escribirFila(Sheet hoja, int indice, String clave, String valor) {
        Row fila = hoja.createRow(indice);
        fila.createCell(0).setCellValue(clave);
        fila.createCell(1).setCellValue(valor);
    }

    /** Escribe valores numéricos y textos conservando sus tipos en Excel. */
    private void poner(Row fila, int indice, Object valor) {
        Cell celda = fila.createCell(indice);
        if (valor == null) return;
        if (valor instanceof Number numero) celda.setCellValue(numero.doubleValue());
        else if (valor instanceof Boolean booleano) celda.setCellValue(booleano);
        else celda.setCellValue(valor.toString());
    }

    /** Activa filtros, fija encabezados y ajusta el ancho de columnas. */
    private void finalizarHoja(Sheet hoja, int columnas) {
        if (hoja.getLastRowNum() >= 0) hoja.setAutoFilter(new org.apache.poi.ss.util.CellRangeAddress(0,
                hoja.getLastRowNum(), 0, columnas - 1));
        hoja.createFreezePane(0, 1);
        for (int i = 0; i < columnas; i++) {
            hoja.autoSizeColumn(i);
            hoja.setColumnWidth(i, Math.min(hoja.getColumnWidth(i), 45 * 256));
        }
    }

    /** Aplica formato de moneda colombiana a las columnas de valores del informe. */
    private void formatearMoneda(Workbook libro, Sheet hoja, int... columnas) {
        CellStyle moneda = libro.createCellStyle();
        moneda.setDataFormat(libro.createDataFormat().getFormat("\"$\" #,##0.00"));
        for (int fila = 1; fila <= hoja.getLastRowNum(); fila++) {
            Row row = hoja.getRow(fila);
            if (row == null) continue;
            for (int columna : columnas) {
                Cell cell = row.getCell(columna);
                if (cell != null && cell.getCellType() == org.apache.poi.ss.usermodel.CellType.NUMERIC) {
                    cell.setCellStyle(moneda);
                }
            }
        }
    }

    /** Determina si un producto coincide con la categoría de informe seleccionada. */
    private boolean coincideFiltroExistencia(String filtro, boolean bajo, boolean agotado,
                                             boolean proximo, boolean vencido) {
        return switch (filtro) {
            case "BAJO" -> bajo;
            case "AGOTADO" -> agotado;
            case "PROXIMO_VENCER" -> proximo;
            case "VENCIDO" -> vencido;
            default -> true;
        };
    }

    /** Comprueba si el medicamento tiene un lote vencido o vigente desde una fecha de corte. */
    private boolean tieneLoteConVencimiento(List<InventarioLote> lotes, LocalDate fecha, boolean vencido) {
        return lotes.stream()
                .anyMatch(lote -> lote.getFechaVencimiento() != null && (vencido
                        ? lote.getFechaVencimiento().isBefore(fecha) : !lote.getFechaVencimiento().isBefore(fecha)));
    }

    /** Comprueba si el medicamento tiene existencias por vencer dentro de treinta días. */
    private boolean tieneLoteProximoAVencer(List<InventarioLote> lotes, LocalDate fecha) {
        return lotes.stream()
                .anyMatch(lote -> lote.getCantidad() != null && lote.getCantidad() > 0
                        && lote.getFechaVencimiento() != null && !lote.getFechaVencimiento().isBefore(fecha)
                        && !lote.getFechaVencimiento().isAfter(fecha.plusDays(30)));
    }

    /** Calcula el valor total de existencias para el costo o precio indicado. */
    private BigDecimal multiplicar(BigDecimal valor, int cantidad) {
        return valor == null ? BigDecimal.ZERO : valor.multiply(BigDecimal.valueOf(cantidad));
    }

    /** Resume el estado del producto para facilitar la revisión del informe. */
    private String estadoInventario(int stock, int minimo, boolean vencido, boolean proximo) {
        if (stock == 0) return "AGOTADO";
        if (vencido) return "CON LOTES VENCIDOS";
        if (proximo) return "PRÓXIMO A VENCER";
        return stock <= minimo ? "STOCK BAJO" : "DISPONIBLE";
    }

    /** Comprueba que el rango de fechas del informe sea válido. */
    private void validarRangoFechas(LocalDate desde, LocalDate hasta) {
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new BadRequestException("La fecha inicial no puede ser posterior a la fecha final.");
        }
    }

    /** Permite la descarga de informes solo a Administración y Despacho Médico. */
    private void validarAccesoInformeInventario() {
        var autenticacion = SecurityContextHolder.getContext().getAuthentication();
        boolean permitido = autenticacion != null && autenticacion.isAuthenticated()
                && autenticacion.getAuthorities().stream().map(authority -> normalizarPerfil(authority.getAuthority()))
                .anyMatch(perfil -> List.of("ROLE_ADMIN", "ROLE_ADMINISTRADOR", "ROLE_DESPACHO_MEDICO")
                        .contains(perfil));
        if (!permitido) throw new BadRequestException("El perfil no tiene permiso para consultar informes de inventario.");
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public MiRespuestaRS crearMedicamento(MedicamentoRq solicitud, String usuarioResponsableId) {
        Usuario usuario = validarOperadorInventario(usuarioResponsableId);
        validarSolicitud(solicitud);
        String nombre = solicitud.getNombre().trim();
        if (medicamentoRepository.existsByNombreIgnoreCase(nombre)) {
            throw new BadRequestException("Ya existe un medicamento con ese nombre.");
        }
        validarDatosInventario(solicitud, null);
        Medicamento medicamento = new Medicamento();
        mapearDatosEditables(medicamento, solicitud);
        medicamento.setCantidadActual(0);
        medicamento.setUsuarioCreacion(usuario);
        medicamento.setFechaCreacionRegistro(LocalDateTime.now());
        medicamento = medicamentoRepository.save(medicamento);
        int saldoInicial = solicitud.getCantidadActual();
        if (saldoInicial > 0) {
            registrarIngresoInicial(medicamento, solicitud, usuario, saldoInicial);
        }
        return respuesta("Medicamento creado correctamente.");
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public MiRespuestaRS actualizarMedicamento(MedicamentoRq solicitud, String usuarioResponsableId) {
        validarOperadorInventario(usuarioResponsableId);
        validarSolicitud(solicitud);
        validarId(solicitud.getId());
        Medicamento medicamento = medicamentoRepository.findLockedById(solicitud.getId())
                .orElseThrow(() -> new BadRequestException("El medicamento no existe."));
        if (medicamentoRepository.existsByNombreIgnoreCaseAndIdNot(solicitud.getNombre().trim(), medicamento.getId())) {
            throw new BadRequestException("Ya existe otro medicamento con ese nombre.");
        }
        validarDatosInventario(solicitud, medicamento.getId());
        if (!solicitud.getCantidadActual().equals(medicamento.getCantidadActual())) {
            throw new BadRequestException("Las existencias solo se modifican mediante entradas, salidas o ajustes trazables.");
        }
        mapearDatosEditables(medicamento, solicitud);
        medicamento.setFechaModificacionRegistro(LocalDateTime.now());
        medicamentoRepository.save(medicamento);
        return respuesta("Medicamento actualizado correctamente.");
    }

    /** Guarda el lote inicial y el movimiento que deja trazabilidad de la entrada. */
    private void registrarIngresoInicial(Medicamento medicamento, MedicamentoRq solicitud, Usuario usuario, int cantidad) {
        InventarioLote lote = new InventarioLote();
        lote.setMedicamento(medicamento);
        lote.setNumeroLote("INICIAL-" + medicamento.getCodigo());
        lote.setCantidad(cantidad);
        lote.setPrecioCompraLote(solicitud.getPrecioCompra());
        lote.setFechaVencimiento(solicitud.getFechaVence());
        lote.setFechaIngreso(solicitud.getFechaCompra().atStartOfDay());
        lote.setActivo(true);
        lote = inventarioLoteRepository.save(lote);
        medicamento.setCantidadActual(cantidad);
        medicamentoRepository.save(medicamento);
        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setMedicamento(medicamento);
        movimiento.setLote(lote);
        movimiento.setTipo("ENTRADA");
        movimiento.setCantidad(cantidad);
        movimiento.setFechaMovimiento(LocalDateTime.now());
        movimiento.setUsuario(usuario);
        movimiento.setObservacion("Saldo inicial de inventario");
        movimientoInventarioRepository.save(movimiento);
    }

    /** Verifica los campos requeridos y la relación entre compra y vencimiento. */
    private void validarSolicitud(MedicamentoRq solicitud) {
        if (solicitud == null || solicitud.getNombre() == null || solicitud.getNombre().isBlank()) {
            throw new BadRequestException("El nombre del medicamento es obligatorio.");
        }
        if (solicitud.getFechaCompra() == null) {
            throw new BadRequestException("La fecha de compra o ingreso es obligatoria.");
        }
        String tipo = normalizarTipo(solicitud.getTipoProducto());
        if ("MEDICAMENTO".equals(tipo) && solicitud.getFechaVence() == null) {
            throw new BadRequestException("La fecha de vencimiento es obligatoria para los medicamentos.");
        }
        if (solicitud.getCantidadActual() != null && solicitud.getCantidadActual() > 0
                && solicitud.getFechaVence() == null) {
            throw new BadRequestException("La fecha de vencimiento es obligatoria para cada lote con existencias.");
        }
        if (solicitud.getFechaVence() != null && solicitud.getFechaVence().isBefore(solicitud.getFechaCompra())) {
            throw new BadRequestException("La fecha de vencimiento no puede ser anterior a la fecha de compra.");
        }
    }

    /** Valida código, tipo de producto, registro Invima, precios y existencias. */
    private void validarDatosInventario(MedicamentoRq solicitud, Long id) {
        if (solicitud.getCodigo() == null || !CODIGO_PRODUCTO.matcher(solicitud.getCodigo().trim()).matches()) {
            throw new BadRequestException("El código del producto debe tener exactamente 8 caracteres alfanuméricos.");
        }
        boolean codigoDuplicado = id == null
                ? medicamentoRepository.existsByCodigoIgnoreCase(solicitud.getCodigo().trim())
                : medicamentoRepository.existsByCodigoIgnoreCaseAndIdNot(solicitud.getCodigo().trim(), id);
        if (codigoDuplicado) {
            throw new BadRequestException("Ya existe un producto con ese código.");
        }
        String tipo = normalizarTipo(solicitud.getTipoProducto());
        if (!List.of("MEDICAMENTO", "PRODUCTO_MEDICO", "UTENSILIO").contains(tipo)) {
            throw new BadRequestException("El tipo debe ser MEDICAMENTO, PRODUCTO_MEDICO o UTENSILIO.");
        }
        if ("MEDICAMENTO".equals(tipo) && (solicitud.getRegistroInvima() == null
                || solicitud.getRegistroInvima().isBlank())) {
            throw new BadRequestException("El registro Invima es obligatorio para los medicamentos.");
        }
        if (solicitud.getRegistroInvima() != null && solicitud.getRegistroInvima().length() > 50) {
            throw new BadRequestException("El registro Invima admite máximo 50 caracteres.");
        }
        validarPrecio(solicitud.getPrecioCompra(), "compra");
        validarPrecio(solicitud.getPrecioVenta(), "venta");
        if (solicitud.getCantidadActual() == null || solicitud.getCantidadActual() < 0) {
            throw new BadRequestException("La cantidad disponible es obligatoria y no puede ser negativa.");
        }
        if (solicitud.getStockMinimo() == null || solicitud.getStockMinimo() < 0) {
            throw new BadRequestException("El stock mínimo es obligatorio y no puede ser negativo.");
        }
    }

    /** Copia los datos editables sin permitir cambios directos de existencias. */
    private void mapearDatosEditables(Medicamento medicamento, MedicamentoRq solicitud) {
        medicamento.setNombre(solicitud.getNombre().trim());
        medicamento.setDescripcion(limpiar(solicitud.getDescripcion()));
        medicamento.setPresentacion(limpiar(solicitud.getPresentacion()));
        medicamento.setFechaCompra(solicitud.getFechaCompra());
        medicamento.setFechaVence(solicitud.getFechaVence());
        medicamento.setCodigo(solicitud.getCodigo().trim().toUpperCase(Locale.ROOT));
        medicamento.setTipoProducto(tipoPersistido(solicitud.getTipoProducto()));
        medicamento.setRegistroInvima(limpiar(solicitud.getRegistroInvima()));
        medicamento.setRequiereInvima("MEDICAMENTO".equals(normalizarTipo(solicitud.getTipoProducto())));
        medicamento.setPrecioVenta(solicitud.getPrecioVenta());
        medicamento.setPrecioCompra(solicitud.getPrecioCompra());
        medicamento.setStockMinimo(solicitud.getStockMinimo());
        medicamento.setFechaProximoPedido(solicitud.getFechaProximoPedido());
    }

    /** Resuelve y valida el usuario responsable de una operación administrativa. */
    private Usuario validarOperadorInventario(String usuarioPrincipal) {
        Long usuarioId;
        try {
            usuarioId = Long.valueOf(usuarioPrincipal);
        } catch (RuntimeException exception) {
            throw new BadRequestException("La identidad autenticada no contiene un id de usuario válido.");
        }
        Usuario usuario = usuarioRepository.findOneById(usuarioId)
                .orElseThrow(() -> new BadRequestException("El usuario responsable no existe."));
        if (!Boolean.TRUE.equals(usuario.getActivo()) || usuario.getPerfil() == null
                || !Boolean.TRUE.equals(usuario.getPerfil().getActivo())) {
            throw new BadRequestException("El usuario no está activo.");
        }
        if (!esAdministrador(usuario.getPerfil().getNombre())) {
            throw new BadRequestException("Solo el perfil ADMIN puede administrar el inventario de medicamentos.");
        }
        return usuario;
    }

    /** Restringe la consulta de medicamentos a administración, médicos y despacho médico. */
    private void validarConsultaMedicamentos() {
        var autenticacion = SecurityContextHolder.getContext().getAuthentication();
        boolean permitido = autenticacion != null && autenticacion.isAuthenticated()
                && autenticacion.getAuthorities().stream().map(authority -> normalizarPerfil(authority.getAuthority()))
                .anyMatch(perfil -> List.of("ROLE_ADMIN", "ROLE_ADMINISTRADOR", "ROLE_MEDICO",
                        "ROLE_DESPACHO_MEDICO").contains(perfil));
        if (!permitido) {
            throw new BadRequestException("El perfil no tiene permiso para consultar medicamentos.");
        }
    }

    /** Normaliza el nombre del perfil eliminando tildes y espacios. */
    private String normalizarPerfil(String nombre) {
        return java.text.Normalizer.normalize(nombre == null ? "" : nombre, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").trim().toUpperCase(Locale.ROOT).replace(' ', '_');
    }

    /** Acepta los nombres de perfil de administrador usados por la base y la matriz. */
    private boolean esAdministrador(String nombre) {
        String perfil = normalizarPerfil(nombre);
        return "ADMIN".equals(perfil) || "ADMINISTRADOR".equals(perfil);
    }

    /** Valida un identificador positivo. */
    private void validarId(Long id) {
        if (id == null || id <= 0) {
            throw new BadRequestException("El id del medicamento debe ser mayor a cero.");
        }
    }

    /** Calcula el saldo utilizable entre el stock general y sus lotes vigentes. */
    private int cantidadDisponible(Medicamento medicamento) {
        long lotesDisponibles = inventarioLoteRepository.buscarLotesDisponibles(
                        medicamento.getId(), LocalDate.now()).stream()
                .mapToLong(lote -> Math.max(0, lote.getCantidad() == null ? 0 : lote.getCantidad()))
                .sum();
        int saldoGeneral = medicamento.getCantidadActual() == null ? 0 : medicamento.getCantidadActual();
        return (int) Math.min(Math.max(0, saldoGeneral), Math.min(Integer.MAX_VALUE, lotesDisponibles));
    }

    /** Valida que un precio exista y no sea negativo. */
    private void validarPrecio(BigDecimal precio, String tipo) {
        if (precio == null || precio.compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("El precio de " + tipo + " es obligatorio y no puede ser negativo.");
        }
    }

    /** Normaliza el texto utilizado para búsquedas. */
    private String normalizarBusqueda(String valor) {
        return valor == null ? "" : valor.trim().toLowerCase(Locale.ROOT);
    }

    /** Normaliza el tipo de producto. */
    private String normalizarTipo(String tipo) {
        return tipo == null ? "" : tipo.trim().toUpperCase(Locale.ROOT);
    }

    /** Convierte categorías de la API al enum admitido por la columna tipo. */
    private String tipoPersistido(String tipo) {
        return "PRODUCTO_MEDICO".equals(normalizarTipo(tipo)) ? "UTENSILIO" : normalizarTipo(tipo);
    }

    /** Comprueba si un campo contiene el término de búsqueda. */
    private boolean contiene(String valor, String busqueda) {
        return valor != null && valor.toLowerCase(Locale.ROOT).contains(busqueda);
    }

    /** Convierte texto vacío a nulo. */
    private String limpiar(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    /** Construye la respuesta estándar de operaciones del inventario. */
    private MiRespuestaRS respuesta(String mensaje) {
        MiRespuestaRS respuesta = new MiRespuestaRS();
        respuesta.setStatus(200);
        respuesta.setMessage(mensaje);
        return respuesta;
    }
}
