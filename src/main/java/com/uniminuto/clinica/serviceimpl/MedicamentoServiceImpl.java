package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.InventarioLote;
import com.uniminuto.clinica.entity.Medicamento;
import com.uniminuto.clinica.entity.MovimientoInventario;
import com.uniminuto.clinica.entity.Usuario;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.MedicamentoRq;
import com.uniminuto.clinica.models.InformeInventarioRs;
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
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import com.uniminuto.clinica.models.ReporteRs;
import com.uniminuto.clinica.util.ExportarReporte;

/** Implementa administración y consultas del inventario de productos médicos. */
@Service
public class MedicamentoServiceImpl implements MedicamentoService {

    private static final Pattern CODIGO_PRODUCTO = Pattern.compile("^[A-Za-z0-9]{8}$");
    private static final int DIAS_PROXIMO_VENCIMIENTO_EXCEL = 30;
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
        return medicamentoRepository.findAllByOrderByNombreAsc().stream()
                .filter(medicamento -> stockBajo(cantidadDisponible(medicamento), medicamento.getStockMinimo()))
                .toList();
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

    /** Consulta el informe de inventario. */
    @Override
    @Transactional(readOnly = true)
    public InformeInventarioRs consultarInformeInventario(String tipo, LocalDate desde, LocalDate hasta) {
        return prepararInformeInventario(tipo, desde, hasta);
    }

    /** Reúne existencias, lotes, movimientos y totales para el informe de inventario. */
    private InformeInventarioRs prepararInformeInventario(String tipo, LocalDate desde, LocalDate hasta) {
        validarAccesoInformeInventario();
        validarRangoFechas(desde, hasta);
        String filtro = tipo == null || tipo.isBlank() ? "TODOS" : tipo.trim().toUpperCase(Locale.ROOT);
        if (!List.of("TODOS", "BAJO", "AGOTADO", "PROXIMO_VENCER", "VENCIDO", "ENTRADAS", "SALIDAS")
                .contains(filtro)) {
            throw new BadRequestException("El tipo de informe debe ser TODOS, BAJO, AGOTADO, PROXIMO_VENCER, VENCIDO, ENTRADAS o SALIDAS.");
        }

        var generado = LocalDateTime.now(ZoneId.of("America/Bogota"));
        var hoy = generado.toLocalDate();
        var lotes = inventarioLoteRepository.findAllByOrderByFechaVencimientoAsc();
        var lotesPorMedicamento = lotes.stream()
                .filter(lote -> lote.getMedicamento() != null && lote.getMedicamento().getId() != null)
                .collect(java.util.stream.Collectors.groupingBy(lote -> lote.getMedicamento().getId()));
        var existencias = prepararExistencias(filtro, lotesPorMedicamento, hoy);
        var detallesLote = prepararLotes(filtro, lotes, hoy);
        var movimientos = prepararMovimientos(filtro, desde, hasta);
        var totales = new InformeInventarioRs.Totales(
                sumarColumna(existencias, "Existencia actual").longValueExact(),
                sumarColumna(existencias, "Disponible para dispensar").longValueExact(),
                sumarColumna(existencias, "Valor inventario costo (COP)"),
                sumarColumna(existencias, "Valor inventario venta (COP)"),
                sumarMovimientos(movimientos, "ENTRADA"),
                sumarMovimientos(movimientos, "SALIDA"));
        return new InformeInventarioRs(generado, filtro, desde, hasta, "COP",
                List.of(existencias, detallesLote, movimientos), totales);
    }

    /** Genera el informe de inventario en un libro de Excel. */
    @Override
    @Transactional(readOnly = true)
    public byte[] exportarInventarioExcel(String tipo, LocalDate desde, LocalDate hasta) {
        var informe = prepararInformeInventario(tipo, desde, hasta);
        String contexto = "Tipo: " + informe.tipo()
                + "  |  Desde (movimientos): " + (desde == null ? "Sin límite" : desde)
                + "  |  Hasta (movimientos): " + (hasta == null ? "Sin límite" : hasta);
        try (var libro = new XSSFWorkbook(); var salida = new ByteArrayOutputStream()) {
            var totales = informe.totales();
            var indicadores = List.of(
                    new ExportarReporte.Indicador("Existencias registradas", totales.existenciasRegistradas(), false),
                    new ExportarReporte.Indicador("Disponible para dispensar", totales.disponiblesParaDispensar(), false),
                    new ExportarReporte.Indicador("Valor al costo", totales.valorCosto(), true),
                    new ExportarReporte.Indicador("Valor potencial de venta", totales.valorPotencialVenta(), true));
            ExportarReporte.hojaExcel(libro, "Parámetros", new ReporteRs("Informe de inventario",
                    informe.generado(), List.of("Parámetro", "Valor"), informe.parametros()), contexto, indicadores);
            for (var seccion : informe.secciones()) {
                ExportarReporte.hojaExcel(libro, seccion.nombre(),
                        new ReporteRs("Inventario · " + seccion.nombre(), informe.generado(),
                                seccion.columnas(), seccion.filas()), contexto, List.of());
            }
            ExportarReporte.hojaExcel(libro, "Totales", new ReporteRs("Inventario · Totales",
                    informe.generado(), List.of("Concepto", "Total"), informe.filasTotales()), contexto, List.of());
            libro.write(salida);
            return salida.toByteArray();
        } catch (IOException exception) {
            throw new BadRequestException("No fue posible generar el informe Excel del inventario.");
        }
    }

    /** Suma los importes de la columna indicada para el total del informe. */
    private BigDecimal sumarColumna(InformeInventarioRs.Seccion seccion, String nombre) {
        int columna = seccion.columnas().indexOf(nombre);
        return seccion.filas().stream().map(fila -> new BigDecimal(fila.get(columna).toString()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** Suma las unidades de los movimientos del tipo indicado. */
    private long sumarMovimientos(InformeInventarioRs.Seccion movimientos, String tipo) {
        int tipoColumna = movimientos.columnas().indexOf("Tipo");
        int cantidadColumna = movimientos.columnas().indexOf("Cantidad");
        return movimientos.filas().stream().filter(fila -> tipo.equalsIgnoreCase((String) fila.get(tipoColumna)))
                .mapToLong(fila -> ((Number) fila.get(cantidadColumna)).longValue()).sum();
    }

    /** Construye el resumen valorizado de existencias y aplica el filtro seleccionado. */
    private InformeInventarioRs.Seccion prepararExistencias(String filtro,
                                      Map<Long, List<InventarioLote>> lotesPorMedicamento, LocalDate hoy) {
        String[] columnas = {"ID", "Código", "Medicamento / producto", "Tipo", "Presentación", "Registro Invima",
                "Existencia actual", "Stock mínimo", "Costo unitario (COP)", "Precio venta (COP)",
                "Valor inventario costo (COP)", "Valor inventario venta (COP)", "Estado",
                "Disponible para dispensar"};
        var filas = new ArrayList<List<Object>>();
        for (Medicamento medicamento : medicamentoRepository.findAllByOrderByNombreAsc()) {
            int stock = medicamento.getCantidadActual() == null ? 0 : medicamento.getCantidadActual();
            int minimo = medicamento.getStockMinimo() == null ? 0 : medicamento.getStockMinimo();
            List<InventarioLote> lotes = lotesPorMedicamento.getOrDefault(medicamento.getId(), List.of());
            int disponible = cantidadDisponible(medicamento, lotes.stream()
                    .filter(lote -> loteDisponible(lote, hoy)).mapToLong(InventarioLote::getCantidad).sum());
            boolean bajo = stockBajo(disponible, minimo);
            boolean agotado = disponible == 0;
            boolean vencido = lotes.stream().anyMatch(lote -> loteVencido(lote, hoy));
            boolean proximo = lotes.stream().anyMatch(lote -> loteProximoAVencer(lote, hoy));
            if (!coincideFiltroExistencia(filtro, bajo, agotado, proximo, vencido)) {
                continue;
            }
            var costoAdquisicion = lotes.stream().filter(this::loteConUnidades)
                    .map(lote -> multiplicar(lote.getPrecioCompraLote(), lote.getCantidad()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            filas.add(java.util.Arrays.asList(
                    medicamento.getId(),
                    medicamento.getCodigo(),
                    medicamento.getNombre(),
                    medicamento.getTipoProducto(),
                    medicamento.getPresentacion(),
                    medicamento.getRegistroInvima(),
                    stock,
                    minimo,
                    medicamento.getPrecioCompra(),
                    medicamento.getPrecioVenta(),
                    costoAdquisicion,
                    multiplicar(medicamento.getPrecioVenta(), disponible),
                    estadoInventario(disponible, minimo, vencido, proximo),
                    disponible));
        }
        return new InformeInventarioRs.Seccion("Existencias", List.of(columnas), filas);
    }

    /** Construye el detalle de lotes con fechas, cantidades y estado de vencimiento. */
    private InformeInventarioRs.Seccion prepararLotes(String filtro, List<InventarioLote> lotes,
                                 LocalDate hoy) {
        String[] columnas = {"ID lote", "ID medicamento", "Medicamento", "Número de lote", "Cantidad",
                "Costo unitario (COP)", "Valor del lote (COP)", "Fecha ingreso", "Fecha vencimiento", "Activo", "Estado"};
        var filas = new ArrayList<List<Object>>();
        for (InventarioLote lote : lotes) {
            boolean expirado = lote.getFechaVencimiento() != null && lote.getFechaVencimiento().isBefore(hoy);
            boolean proximo = loteProximoAVencer(lote, hoy);
            if ("VENCIDO".equals(filtro) && !loteVencido(lote, hoy)
                    || "PROXIMO_VENCER".equals(filtro) && !proximo) {
                continue;
            }
            int cantidad = lote.getCantidad() == null ? 0 : lote.getCantidad();
            filas.add(java.util.Arrays.asList(
                    lote.getId(),
                    lote.getMedicamento() == null ? null : lote.getMedicamento().getId(),
                    lote.getMedicamento() == null ? null : lote.getMedicamento().getNombre(),
                    lote.getNumeroLote(),
                    cantidad,
                    lote.getPrecioCompraLote(),
                    multiplicar(lote.getPrecioCompraLote(), cantidad),
                    lote.getFechaIngreso(),
                    lote.getFechaVencimiento(),
                    Boolean.TRUE.equals(lote.getActivo()) ? "Sí" : "No",
                    expirado ? "VENCIDO" : proximo ? "PRÓXIMO A VENCER" : "VIGENTE"));
        }
        return new InformeInventarioRs.Seccion("Lotes", List.of(columnas), filas);
    }

    /** Construye el historial de movimientos y aplica el rango de fechas solicitado. */
    private InformeInventarioRs.Seccion prepararMovimientos(String filtro,
                                       LocalDate desde, LocalDate hasta) {
        String[] columnas = {"ID movimiento", "Fecha", "Tipo", "ID medicamento", "Medicamento", "ID lote",
                "Lote", "Cantidad", "Usuario responsable", "Fórmula", "Despacho", "Observación"};
        var filas = new ArrayList<List<Object>>();
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
            filas.add(java.util.Arrays.asList(
                    movimiento.getId(),
                    movimiento.getFechaMovimiento(),
                    movimiento.getTipo(),
                    movimiento.getMedicamento() == null ? null : movimiento.getMedicamento().getId(),
                    movimiento.getMedicamento() == null ? null : movimiento.getMedicamento().getNombre(),
                    movimiento.getLote() == null ? null : movimiento.getLote().getId(),
                    movimiento.getLote() == null ? null : movimiento.getLote().getNumeroLote(),
                    movimiento.getCantidad(),
                    movimiento.getUsuario() == null ? null : movimiento.getUsuario().getUsername(),
                    movimiento.getFormula() == null ? null : movimiento.getFormula().getId(),
                    movimiento.getDespacho() == null ? null : movimiento.getDespacho().getNumeroDespacho(),
                    movimiento.getObservacion()));
        }
        return new InformeInventarioRs.Seccion("Movimientos", List.of(columnas), filas);
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

    /** Comprueba si el lote conserva unidades en inventario. */
    private boolean loteConUnidades(InventarioLote lote) {
        return lote.getCantidad() != null && lote.getCantidad() > 0;
    }

    /** Comprueba si el lote está activo, tiene unidades y sigue vigente. */
    private boolean loteDisponible(InventarioLote lote, LocalDate fecha) {
        return Boolean.TRUE.equals(lote.getActivo()) && loteConUnidades(lote)
                && (lote.getFechaVencimiento() == null || !lote.getFechaVencimiento().isBefore(fecha));
    }

    /** Comprueba si un lote con unidades ya superó su fecha de vencimiento. */
    private boolean loteVencido(InventarioLote lote, LocalDate fecha) {
        return loteConUnidades(lote) && lote.getFechaVencimiento() != null
                && lote.getFechaVencimiento().isBefore(fecha);
    }

    /** Comprueba si el lote disponible vence dentro del período de alerta. */
    private boolean loteProximoAVencer(InventarioLote lote, LocalDate fecha) {
        return loteDisponible(lote, fecha) && lote.getFechaVencimiento() != null
                && !lote.getFechaVencimiento().isAfter(fecha.plusDays(DIAS_PROXIMO_VENCIMIENTO_EXCEL));
    }

    /** Determina si la cantidad disponible está en el nivel mínimo o por debajo. */
    private boolean stockBajo(int disponible, Integer minimo) {
        return disponible > 0 && minimo != null && disponible <= minimo;
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
        return stockBajo(stock, minimo) ? "STOCK BAJO" : "DISPONIBLE";
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
        validarProximoPedido(solicitud.getFechaProximoPedido(), null);
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
        validarProximoPedido(solicitud.getFechaProximoPedido(), medicamento.getFechaProximoPedido());
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
        if (solicitud.getFechaVence() != null && solicitud.getFechaVence().isBefore(solicitud.getFechaCompra())) {
            throw new BadRequestException("La fecha de vencimiento no puede ser anterior a la fecha de compra.");
        }
    }

    /** Impide asignar un próximo pedido anterior a hoy y permite conservar la fecha registrada. */
    private void validarProximoPedido(LocalDate fecha, LocalDate anterior) {
        if (fecha != null && !fecha.equals(anterior) && fecha.isBefore(LocalDate.now(ZoneId.of("America/Bogota")))) {
            throw new BadRequestException("El próximo pedido debe ser de hoy o posterior.");
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
        return cantidadDisponible(medicamento, lotesDisponibles);
    }

    /** Calcula las unidades dispensables usando el saldo del producto y los lotes vigentes. */
    private int cantidadDisponible(Medicamento medicamento, long lotesDisponibles) {
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
