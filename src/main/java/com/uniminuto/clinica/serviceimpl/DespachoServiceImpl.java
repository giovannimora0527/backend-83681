package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.entity.Despacho;
import com.uniminuto.clinica.entity.DespachoDetalle;
import com.uniminuto.clinica.entity.DespachoHistorialEstado;
import com.uniminuto.clinica.entity.FormulaDetalle;
import com.uniminuto.clinica.entity.FormulaMedica;
import com.uniminuto.clinica.entity.InventarioLote;
import com.uniminuto.clinica.entity.Medicamento;
import com.uniminuto.clinica.entity.MovimientoInventario;
import com.uniminuto.clinica.entity.Usuario;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.DespachoCancelarRq;
import com.uniminuto.clinica.models.DespachoRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.repository.CitaRepository;
import com.uniminuto.clinica.repository.DespachoDetalleRepository;
import com.uniminuto.clinica.repository.DespachoHistorialEstadoRepository;
import com.uniminuto.clinica.repository.DespachoRepository;
import com.uniminuto.clinica.repository.FormulaDetalleRepository;
import com.uniminuto.clinica.repository.FormulaMedicaRepository;
import com.uniminuto.clinica.repository.InventarioLoteRepository;
import com.uniminuto.clinica.repository.MedicamentoRepository;
import com.uniminuto.clinica.repository.MovimientoInventarioRepository;
import com.uniminuto.clinica.repository.UsuarioRepository;
import com.uniminuto.clinica.service.DespachoService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

/** Implementa el despacho transaccional y la actualización de existencias por lote. */
@Service
public class DespachoServiceImpl implements DespachoService {

    private static final String ESTADO_PENDIENTE = "PENDIENTE";
    private static final String ESTADO_EN_PREPARACION = "EN_PREPARACION";
    private static final String ESTADO_PARCIAL = "PARCIAL";
    private static final String ESTADO_DESPACHADO = "DESPACHADO";
    private static final String ESTADO_PENDIENTE_DISPONIBILIDAD = "PENDIENTE_DISPONIBILIDAD";
    private static final String ESTADO_CANCELADO = "CANCELADO";

    private final DespachoRepository despachoRepository;
    private final DespachoDetalleRepository despachoDetalleRepository;
    private final DespachoHistorialEstadoRepository despachoHistorialEstadoRepository;
    private final FormulaDetalleRepository formulaDetalleRepository;
    private final FormulaMedicaRepository formulaMedicaRepository;
    private final InventarioLoteRepository inventarioLoteRepository;
    private final MedicamentoRepository medicamentoRepository;
    private final MovimientoInventarioRepository movimientoInventarioRepository;
    private final UsuarioRepository usuarioRepository;
    private final CitaRepository citaRepository;

    /** Construye el servicio con los repositorios de fórmulas, despacho e inventario. */
    public DespachoServiceImpl(
            DespachoRepository despachoRepository,
            DespachoDetalleRepository despachoDetalleRepository,
            DespachoHistorialEstadoRepository despachoHistorialEstadoRepository,
            FormulaDetalleRepository formulaDetalleRepository,
            FormulaMedicaRepository formulaMedicaRepository,
            InventarioLoteRepository inventarioLoteRepository,
            MedicamentoRepository medicamentoRepository,
            MovimientoInventarioRepository movimientoInventarioRepository,
            UsuarioRepository usuarioRepository,
            CitaRepository citaRepository) {
        this.despachoRepository = despachoRepository;
        this.despachoDetalleRepository = despachoDetalleRepository;
        this.despachoHistorialEstadoRepository = despachoHistorialEstadoRepository;
        this.formulaDetalleRepository = formulaDetalleRepository;
        this.formulaMedicaRepository = formulaMedicaRepository;
        this.inventarioLoteRepository = inventarioLoteRepository;
        this.medicamentoRepository = medicamentoRepository;
        this.movimientoInventarioRepository = movimientoInventarioRepository;
        this.usuarioRepository = usuarioRepository;
        this.citaRepository = citaRepository;
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public List<Despacho> listarDespachos(String usuarioResponsableId) {
        validarUsuarioDespacho(usuarioResponsableId);
        return despachoRepository.findAllByOrderByIdDesc();
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public Despacho consultarDespacho(Long despachoId, String usuarioResponsableId) {
        validarUsuarioDespacho(usuarioResponsableId);
        return obtenerDespacho(despachoId);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public List<Despacho> listarDespachosPorFormula(Long formulaId, String usuarioResponsableId) {
        validarUsuarioDespacho(usuarioResponsableId);
        validarId(formulaId, "fórmula");
        return despachoRepository.findByFormulaIdOrderByIdDesc(formulaId);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public List<DespachoDetalle> listarDetalles(Long despachoId, String usuarioResponsableId) {
        validarUsuarioDespacho(usuarioResponsableId);
        obtenerDespacho(despachoId);
        return enriquecerDetalles(despachoDetalleRepository.findByDespacho_IdOrderByIdAsc(despachoId));
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public List<DespachoHistorialEstado> listarHistorial(Long despachoId, String usuarioResponsableId) {
        validarUsuarioDespacho(usuarioResponsableId);
        obtenerDespacho(despachoId);
        return despachoHistorialEstadoRepository.findByDespacho_IdOrderByFechaCambioAsc(despachoId);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public List<FormulaMedica> listarFormulasPendientes(
            String filtro, LocalDate fechaGeneracion, String usuarioResponsableId) {
        validarUsuarioDespacho(usuarioResponsableId);
        return formulaMedicaRepository.buscarFormulasPendientesDespacho(limpiar(filtro), fechaGeneracion)
                .stream()
                .map(this::enriquecerFormulaPendiente)
                .toList();
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public FormulaMedica consultarFormulaParaDespacho(Long formulaId, String usuarioResponsableId) {
        validarUsuarioDespacho(usuarioResponsableId);
        validarId(formulaId, "fórmula");
        FormulaMedica formula = enriquecerFormulaPendiente(formulaId);
        validarCitaParaEntrega(formula.getCita().getEstado());
        if (formula.getDetallesDespacho().stream().noneMatch(detalle -> detalle.getCantidadPendiente() > 0)) {
            throw new BadRequestException("La fórmula ya no tiene cantidades pendientes de entrega.");
        }
        return formula;
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Despacho prepararDespacho(DespachoRq despachoRq, String usuarioResponsableId) {
        if (despachoRq == null) {
            throw new BadRequestException("La información del despacho es obligatoria.");
        }
        validarId(despachoRq.getFormulaId(), "fórmula");
        Usuario usuario = validarUsuarioDespacho(usuarioResponsableId);
        bloquearCitaParaEntrega(despachoRq.getFormulaId());
        Long formulaId = bloquearFormulaVigente(despachoRq.getFormulaId());
        List<FormulaDetalle> detallesFormula = formulaDetalleRepository.findByFormulaIdOrderByIdAsc(formulaId);
        if (detallesFormula.isEmpty()) {
            throw new BadRequestException("La fórmula no tiene medicamentos formulados para despachar.");
        }
        if (!tieneCantidadesPendientes(detallesFormula)) {
            throw new BadRequestException("La fórmula ya fue despachada completamente.");
        }

        List<String> estadosActivos = List.of(ESTADO_EN_PREPARACION, ESTADO_PARCIAL,
                ESTADO_PENDIENTE_DISPONIBILIDAD);
        Optional<Despacho> existente = despachoRepository
                .findFirstByFormulaIdAndEstadoInOrderByIdDesc(formulaId, estadosActivos);
        if (existente.isPresent()) {
            Despacho despacho = existente.get();
            if (ESTADO_PENDIENTE_DISPONIBILIDAD.equals(despacho.getEstado())) {
                String estadoAnterior = despacho.getEstado();
                despacho.setEstado(ESTADO_EN_PREPARACION);
                despacho = despachoRepository.save(despacho);
                guardarHistorial(despacho, estadoAnterior, ESTADO_EN_PREPARACION, usuario,
                        limpiar(despachoRq.getObservacion()), LocalDateTime.now());
            }
            return enriquecerDespacho(despacho);
        }

        LocalDateTime ahora = LocalDateTime.now();
        Despacho despacho = new Despacho();
        despacho.setNumeroDespacho(generarNumeroDespacho());
        despacho.setFormulaId(formulaId);
        despacho.setUsuarioDespacho(usuario);
        despacho.setEstado(ESTADO_EN_PREPARACION);
        despacho.setObservacion(limpiar(despachoRq.getObservacion()));
        completarDatosCita(despacho, formulaId);
        despacho = despachoRepository.save(despacho);
        guardarHistorial(despacho, ESTADO_PENDIENTE, ESTADO_EN_PREPARACION,
                usuario, despacho.getObservacion(), ahora);
        return enriquecerDespacho(despacho);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Despacho confirmarDespacho(Long despachoId, List<LineaConfirmacion> detalles,
                                      String claveIdempotencia, String observacion,
                                      String usuarioResponsableId) {
        Usuario usuario = validarUsuarioDespacho(usuarioResponsableId);
        validarId(despachoId, "despacho");
        Long formulaOrigen = despachoRepository.buscarFormulaId(despachoId)
                .orElseThrow(() -> new BadRequestException("El despacho no existe."));
        bloquearCitaParaEntrega(formulaOrigen);
        Despacho despacho = despachoRepository.findLockedById(despachoId)
                .orElseThrow(() -> new BadRequestException("El despacho no existe."));
        if (!List.of(ESTADO_EN_PREPARACION, ESTADO_PARCIAL,
                ESTADO_PENDIENTE_DISPONIBILIDAD).contains(despacho.getEstado())) {
            throw new BadRequestException("El despacho no está en un estado que permita confirmar entregas.");
        }
        String clave = validarClaveIdempotencia(claveIdempotencia);
        boolean claveYaProcesada = despachoHistorialEstadoRepository
                .findByDespacho_IdOrderByFechaCambioAsc(despachoId).stream()
                .anyMatch(evento -> evento.getObservacion() != null
                        && evento.getObservacion().startsWith("IDEMPOTENCIA:" + clave + "|"));
        if (claveYaProcesada) {
            throw new BadRequestException("La confirmación ya fue procesada para esta clave de idempotencia.");
        }

        Long formulaId = bloquearFormulaVigente(despacho.getFormulaId());
        List<FormulaDetalle> formulas = formulaDetalleRepository.findByFormulaIdOrderByIdAsc(formulaId);
        if (formulas.isEmpty()) {
            throw new BadRequestException("La fórmula no tiene medicamentos formulados para despachar.");
        }
        Map<Long, FormulaDetalle> formulaPorDetalle = new HashMap<>();
        Map<Long, Long> pendientePorDetalle = new HashMap<>();
        long pendienteTotal = 0;
        for (FormulaDetalle formulaDetalle : formulas) {
            validarDetalleFormula(formulaDetalle);
            formulaPorDetalle.put(formulaDetalle.getId(), formulaDetalle);
            long entregado = cantidadEntregadaFormula(formulaDetalle.getId());
            long pendiente = formulaDetalle.getCantidadPrescrita() - entregado;
            if (pendiente < 0) {
                throw new BadRequestException("La cantidad despachada supera la cantidad prescrita.");
            }
            pendientePorDetalle.put(formulaDetalle.getId(), pendiente);
            pendienteTotal += pendiente;
        }
        if (pendienteTotal == 0) {
            throw new BadRequestException("La fórmula ya fue despachada completamente.");
        }

        List<LineaConfirmacion> lineas = detalles == null ? List.of() : detalles;
        Map<Long, Medicamento> medicamentos = bloquearMedicamentos(formulas);
        Map<Long, List<InventarioLote>> lotesPorMedicamento = cargarLotesDisponibles(medicamentos.keySet());
        Map<Long, InventarioLote> lotesBloqueados = new TreeMap<>();
        lotesPorMedicamento.values().forEach(lotes -> lotes.forEach(lote -> lotesBloqueados.put(lote.getId(), lote)));
        if (lineas.isEmpty() && hayExistenciasElegibles(formulas)) {
            throw new BadRequestException("Hay lotes vigentes disponibles; seleccione las cantidades que se entregarán.");
        }
        Map<Long, Long> cantidadPorDetalle = new HashMap<>();
        Map<Long, Long> cantidadPorLote = new HashMap<>();
        Set<String> selecciones = new HashSet<>();
        long cantidadEntregadaTotal = 0;
        for (LineaConfirmacion linea : lineas) {
            if (linea == null || linea.getFormulaDetalleId() == null || linea.getLoteId() == null
                    || linea.getCantidadEntregada() == null || linea.getCantidadEntregada() <= 0) {
                throw new BadRequestException("Cada línea debe incluir detalle, lote y cantidad positiva.");
            }
            String seleccion = linea.getFormulaDetalleId() + ":" + linea.getLoteId();
            if (!selecciones.add(seleccion)) {
                throw new BadRequestException("No repita el mismo lote para una línea de fórmula.");
            }
            FormulaDetalle formulaDetalle = formulaPorDetalle.get(linea.getFormulaDetalleId());
            if (formulaDetalle == null) {
                throw new BadRequestException("Una línea seleccionada no pertenece a la fórmula del despacho.");
            }
            long acumulado = cantidadPorDetalle.merge(
                    formulaDetalle.getId(), linea.getCantidadEntregada().longValue(), Math::addExact);
            if (acumulado > pendientePorDetalle.get(formulaDetalle.getId())) {
                throw new BadRequestException("No se puede entregar más de la cantidad pendiente de la fórmula.");
            }
            InventarioLote lote = lotesBloqueados.get(linea.getLoteId());
            if (lote == null) {
                throw new BadRequestException("El lote seleccionado está vencido, inactivo o no tiene existencias.");
            }
            if (!lote.getMedicamento().getId().equals(formulaDetalle.getMedicamentoId())) {
                throw new BadRequestException("El lote seleccionado no corresponde al medicamento formulado.");
            }
            if (!Boolean.TRUE.equals(lote.getActivo()) || lote.getCantidad() == null || lote.getCantidad() <= 0) {
                throw new BadRequestException("El lote seleccionado no tiene existencias disponibles.");
            }
            if (lote.getFechaVencimiento().isBefore(LocalDate.now())) {
                throw new BadRequestException("No se permite dispensar medicamentos vencidos.");
            }
            long cantidadLote = cantidadPorLote.merge(
                    lote.getId(), linea.getCantidadEntregada().longValue(), Math::addExact);
            if (cantidadLote > lote.getCantidad()) {
                throw new BadRequestException("La cantidad a entregar supera las existencias del lote.");
            }
            cantidadEntregadaTotal += linea.getCantidadEntregada();
        }
        validarPrioridadFefo(formulas, cantidadPorDetalle, cantidadPorLote, lotesPorMedicamento);

        LocalDateTime ahora = LocalDateTime.now();
        String estadoAnterior = despacho.getEstado();
        for (LineaConfirmacion linea : lineas) {
            FormulaDetalle formulaDetalle = formulaPorDetalle.get(linea.getFormulaDetalleId());
            InventarioLote lote = lotesBloqueados.get(linea.getLoteId());
            Medicamento medicamento = medicamentos.get(formulaDetalle.getMedicamentoId());
            lote.setCantidad(lote.getCantidad() - linea.getCantidadEntregada());
            inventarioLoteRepository.save(lote);

            DespachoDetalle detalle = new DespachoDetalle();
            detalle.setDespacho(despacho);
            detalle.setFormulaDetalleId(formulaDetalle.getId());
            detalle.setMedicamento(medicamento);
            detalle.setLote(lote);
            detalle.setCantidadEntregada(linea.getCantidadEntregada());
            detalle.setCantidadFormulada(formulaDetalle.getCantidadPrescrita());
            detalle = despachoDetalleRepository.save(detalle);
            guardarMovimientoSalida(despacho, detalle,
                    new AsignacionLote(formulaDetalle, lote, linea.getCantidadEntregada()), usuario, ahora);
        }

        Map<Long, Integer> cantidadesPorMedicamento = new TreeMap<>();
        for (FormulaDetalle formulaDetalle : formulas) {
            long cantidad = cantidadPorDetalle.getOrDefault(formulaDetalle.getId(), 0L);
            if (cantidad > 0) {
                cantidadesPorMedicamento.merge(formulaDetalle.getMedicamentoId(), Math.toIntExact(cantidad),
                        Math::addExact);
            }
        }
        actualizarStockMedicamentos(medicamentos, cantidadesPorMedicamento);

        String nuevoEstado = determinarEstado(formulas, pendientePorDetalle, cantidadPorDetalle,
                lotesPorMedicamento, cantidadEntregadaTotal);
        despacho.setEstado(nuevoEstado);
        despacho.setUsuarioDespacho(usuario);
        if (cantidadEntregadaTotal > 0) {
            despacho.setFechaDespacho(ahora);
            despacho.setNumeroComprobante(generarNumeroComprobante());
            despacho.setFechaGeneracionComprobante(ahora);
            despacho.setUrlComprobantePdf("/inventario/dispensaciones/" + despacho.getId() + "/comprobante");
        }
        String observacionLimpia = limpiar(observacion);
        if (observacionLimpia != null) {
            despacho.setObservacion(observacionLimpia);
        }
        despacho = despachoRepository.save(despacho);
        String registroConfirmacion = "IDEMPOTENCIA:" + clave + "|"
                + (observacionLimpia == null ? "Confirmación de entrega" : observacionLimpia);
        guardarHistorial(despacho, estadoAnterior, nuevoEstado, usuario, registroConfirmacion, ahora);
        return enriquecerDespacho(despacho);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public MiRespuestaRS cancelarDespacho(
            Long despachoId,
            DespachoCancelarRq cancelacionRq,
            String usuarioResponsableId) {
        Usuario usuario = validarUsuarioDespacho(usuarioResponsableId);
        Despacho despacho = despachoRepository.findLockedById(validarId(despachoId, "despacho"))
                .orElseThrow(() -> new BadRequestException("El despacho no existe."));
        if (ESTADO_CANCELADO.equals(despacho.getEstado())) {
            throw new BadRequestException("El despacho ya está cancelado.");
        }
        if (!despachoDetalleRepository.buscarFacturasVigentes(despachoId).isEmpty()) {
            throw new BadRequestException("Anula la factura vigente antes de revertir el despacho.");
        }

        String observacion = limpiar(cancelacionRq == null ? null : cancelacionRq.getObservacion());
        if (observacion == null) {
            throw new BadRequestException("La cancelación requiere una justificación para mantener la trazabilidad.");
        }
        List<DespachoDetalle> detalles = despachoDetalleRepository.findByDespacho_IdOrderByIdAsc(despachoId);
        detalles.sort((primero, segundo) -> {
            int porMedicamento = primero.getMedicamento().getId().compareTo(segundo.getMedicamento().getId());
            return porMedicamento != 0
                    ? porMedicamento
                    : primero.getLote().getId().compareTo(segundo.getLote().getId());
        });
        Map<Long, Medicamento> medicamentos = new TreeMap<>();
        for (DespachoDetalle detalle : detalles) {
            Long medicamentoId = detalle.getMedicamento().getId();
            if (!medicamentos.containsKey(medicamentoId)) {
                Medicamento medicamento = medicamentoRepository.findLockedById(medicamentoId)
                        .orElseThrow(() -> new BadRequestException("El medicamento del despacho ya no existe."));
                medicamentos.put(medicamentoId, medicamento);
            }
        }
        for (DespachoDetalle detalle : detalles) {
            revertirDetalle(despacho, detalle, medicamentos.get(detalle.getMedicamento().getId()),
                    usuario, observacion);
        }

        String estadoAnterior = despacho.getEstado();
        despacho.setEstado(ESTADO_CANCELADO);
        despacho.setObservacion(observacion == null ? despacho.getObservacion() : observacion);
        despachoRepository.save(despacho);
        guardarHistorial(despacho, estadoAnterior, ESTADO_CANCELADO, usuario, observacion, LocalDateTime.now());
        return respuesta(detalles.isEmpty()
                ? "Preparación de despacho cancelada sin afectar el inventario."
                : "Despacho cancelado y cantidades devueltas al inventario.");
    }

    @Override
    @Transactional
    public MiRespuestaRS cerrarPendientes(Long despachoId, DespachoCancelarRq solicitud, String usuarioResponsableId) {
        Usuario usuario = validarUsuarioDespacho(usuarioResponsableId);
        String observacion = limpiar(solicitud == null ? null : solicitud.getObservacion());
        if (observacion == null) {
            throw new BadRequestException("Justifica por qué el cliente no retirará las cantidades pendientes.");
        }
        Despacho despacho = despachoRepository.findLockedById(validarId(despachoId, "despacho"))
                .orElseThrow(() -> new BadRequestException("El despacho no existe."));
        if (!ESTADO_PARCIAL.equals(despacho.getEstado()) || despacho.getFechaDespacho() == null) {
            throw new BadRequestException("Se requiere un despacho parcial confirmado.");
        }
        long formulaId = bloquearFormulaVigente(despacho.getFormulaId());
        var detalles = formulaDetalleRepository.findByFormulaIdOrderByIdAsc(formulaId);
        if (!tieneCantidadesPendientes(detalles)) {
            throw new BadRequestException("La fórmula ya no tiene cantidades pendientes.");
        }
        FormulaMedica formula = formulaMedicaRepository.findById(formulaId)
                .orElseThrow(() -> new BadRequestException("La fórmula no existe."));
        var ahora = LocalDateTime.now();
        formula.setEstado("COMPLETA");
        formula.setFechaActualizacionRegistro(ahora);
        formulaMedicaRepository.save(formula);
        guardarHistorial(despacho, ESTADO_PARCIAL, ESTADO_PARCIAL, usuario,
                "Cierre de cantidades pendientes: " + observacion, ahora);
        return respuesta("Se cerraron las cantidades pendientes sin modificar las entregas registradas.");
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public List<Despacho> consultarHistorialDespachos(String filtro, LocalDate fechaDesde, LocalDate fechaHasta,
                                                       String usuarioResponsableId) {
        validarUsuarioDespacho(usuarioResponsableId);
        if (fechaDesde != null && fechaHasta != null && fechaDesde.isAfter(fechaHasta)) {
            throw new BadRequestException("La fecha inicial no puede ser posterior a la fecha final.");
        }
        String texto = limpiar(filtro);
        return despachoRepository.findAllByOrderByIdDesc().stream()
                .map(this::enriquecerDespacho)
                .filter(despacho -> coincideFiltroHistorial(despacho, texto))
                .filter(despacho -> coincideRangoFechas(despacho, fechaDesde, fechaHasta))
                .toList();
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public List<DespachoDetalle> consultarHistorialMedicamento(Long medicamentoId, String usuarioResponsableId) {
        validarUsuarioDespacho(usuarioResponsableId);
        validarId(medicamentoId, "medicamento");
        if (!medicamentoRepository.existsById(medicamentoId)) {
            throw new BadRequestException("El medicamento no existe.");
        }
        return enriquecerDetalles(despachoDetalleRepository.buscarHistorialPorMedicamento(medicamentoId));
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public byte[] generarComprobantePdf(Long despachoId, String usuarioResponsableId) {
        validarUsuarioDespacho(usuarioResponsableId);
        Despacho despacho = enriquecerDespacho(obtenerDespacho(despachoId));
        if (despacho.getDetallesTrazabilidad() == null || despacho.getDetallesTrazabilidad().isEmpty()) {
            throw new BadRequestException("El despacho todavía no tiene entregas confirmadas para generar comprobante.");
        }

        List<String> lineas = new ArrayList<>();
        lineas.add("COMPROBANTE DE DISPENSACION DE MEDICAMENTOS");
        lineas.add("Numero de comprobante: " + despacho.getNumeroComprobante());
        lineas.add("Numero de despacho: " + despacho.getNumeroDespacho());
        lineas.add("Formula: " + despacho.getNumeroFormula());
        lineas.add("Fecha y hora de entrega: " + despacho.getFechaDespacho());
        lineas.add("Veterinario: " + despacho.getMedicoNombre());
        lineas.add("Propietario: " + despacho.getPropietarioNombre());
        lineas.add("Mascota: " + despacho.getMascotaNombre());
        lineas.add("Usuario responsable: " + (despacho.getUsuarioDespacho() == null
                ? "" : despacho.getUsuarioDespacho().getUsername()));
        lineas.add("Estado del despacho: " + despacho.getEstado());
        lineas.add("Detalle de medicamentos entregados:");
        for (DespachoDetalle detalle : despacho.getDetallesTrazabilidad()) {
            lineas.add("- " + detalle.getMedicamentoNombre() + " | Lote " + detalle.getNumeroLote()
                    + " | Vence " + detalle.getFechaVencimientoLote()
                    + " | Entregado " + detalle.getCantidadEntregada()
                    + " de " + detalle.getCantidadFormulada());
        }
        lineas.add("Observaciones: " + (despacho.getObservacion() == null ? "" : despacho.getObservacion()));

        try (PDDocument documento = new PDDocument(); ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            PDPage pagina = new PDPage(PDRectangle.LETTER);
            documento.addPage(pagina);
            PDPageContentStream contenido = new PDPageContentStream(documento, pagina);
            float posicionY = 740;
            for (String linea : lineas) {
                String texto = limpiarTextoPdf(linea);
                int longitud = Math.max(1, texto.length());
                for (int inicio = 0; inicio < longitud; inicio += 95) {
                    if (posicionY < 50) {
                        contenido.close();
                        pagina = new PDPage(PDRectangle.LETTER);
                        documento.addPage(pagina);
                        contenido = new PDPageContentStream(documento, pagina);
                        posicionY = 740;
                    }
                    String segmento = texto.substring(inicio, Math.min(inicio + 95, texto.length()));
                    contenido.beginText();
                    contenido.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
                    contenido.newLineAtOffset(50, posicionY);
                    contenido.showText(segmento);
                    contenido.endText();
                    posicionY -= 16;
                }
            }
            contenido.close();
            documento.save(salida);
            return salida.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("No fue posible generar el comprobante PDF del despacho.", exception);
        }
    }

    private FormulaMedica enriquecerFormulaPendiente(Long formulaId) {
        FormulaMedica formula = formulaMedicaRepository.findById(formulaId)
                .orElseThrow(() -> new BadRequestException("La fórmula médica no existe."));
        if (!Boolean.TRUE.equals(formula.getActivo()) || !"VIGENTE".equals(formula.getEstado())) {
            throw new BadRequestException("La fórmula está inactiva o no está vigente.");
        }
        Cita cita = formula.getCita();
        if (cita == null || cita.getCliente() == null || cita.getMascota() == null) {
            throw new BadRequestException("La fórmula no tiene propietario y mascota asociados a su cita.");
        }
        formula.setMedicoNombre(formula.getMedico() == null ? null
                : nombreCompleto(formula.getMedico().getNombres(), formula.getMedico().getApellidos()));
        formula.setPropietarioId(cita.getCliente().getId());
        formula.setPropietarioNombre(nombreCompleto(cita.getCliente().getNombres(), cita.getCliente().getApellidos()));
        formula.setMascotaId(cita.getMascota().getMascotaId().longValue());
        formula.setMascotaNombre(cita.getMascota().getNombreMascota());

        List<FormulaDetalle> detalles = formulaDetalleRepository.findByFormulaIdOrderByIdAsc(formulaId);
        LocalDate hoy = LocalDate.now();
        for (FormulaDetalle detalle : detalles) {
            validarDetalleFormula(detalle);
            Medicamento medicamento = medicamentoRepository.findById(detalle.getMedicamentoId())
                    .orElseThrow(() -> new BadRequestException("Un medicamento de la fórmula no existe."));
            long yaEntregado = cantidadEntregadaFormula(detalle.getId());
            long pendiente = detalle.getCantidadPrescrita() - yaEntregado;
            if (pendiente < 0) {
                throw new BadRequestException("La cantidad despachada supera la cantidad prescrita.");
            }
            List<InventarioLote> lotes = inventarioLoteRepository
                    .findByMedicamento_IdOrderByFechaVencimientoAsc(detalle.getMedicamentoId()).stream()
                    .filter(lote -> Boolean.TRUE.equals(lote.getActivo())
                            && lote.getCantidad() != null && lote.getCantidad() > 0)
                    .toList();
            long disponible = lotes.stream()
                    .filter(lote -> !lote.getFechaVencimiento().isBefore(hoy))
                    .mapToLong(lote -> lote.getCantidad())
                    .sum();
            detalle.setMedicamentoNombre(medicamento.getNombre());
            detalle.setCantidadDispensada(Math.toIntExact(yaEntregado));
            detalle.setCantidadPendiente(Math.toIntExact(pendiente));
            detalle.setCantidadDisponible(Math.toIntExact(disponible));
            detalle.setLotesDisponibles(lotes);
        }
        formula.setDetallesDespacho(detalles);
        formula.setEstadoDispensacion(obtenerEstadoDispensacion(formulaId, detalles));
        return formula;
    }

    private String obtenerEstadoDispensacion(Long formulaId, List<FormulaDetalle> detalles) {
        List<Despacho> despachos = despachoRepository.findByFormulaIdOrderByIdDesc(formulaId);
        for (Despacho despacho : despachos) {
            if (ESTADO_EN_PREPARACION.equals(despacho.getEstado())) {
                return ESTADO_EN_PREPARACION;
            }
        }
        for (Despacho despacho : despachos) {
            if (ESTADO_PARCIAL.equals(despacho.getEstado())) {
                return "DESPACHO_PARCIAL";
            }
        }
        for (Despacho despacho : despachos) {
            if (ESTADO_PENDIENTE_DISPONIBILIDAD.equals(despacho.getEstado())) {
                return ESTADO_PENDIENTE_DISPONIBILIDAD;
            }
        }
        boolean quedanPendientes = detalles.stream().anyMatch(detalle -> detalle.getCantidadPendiente() > 0);
        return quedanPendientes ? ESTADO_PENDIENTE : ESTADO_DESPACHADO;
    }

    private Despacho enriquecerDespacho(Despacho despacho) {
        completarDatosHistorial(despacho);
        despacho.setDetallesTrazabilidad(enriquecerDetalles(
                despachoDetalleRepository.findByDespacho_IdOrderByIdAsc(despacho.getId())));
        return despacho;
    }

    private void completarDatosHistorial(Despacho despacho) {
        formulaMedicaRepository.findById(despacho.getFormulaId()).ifPresent(formula -> {
            despacho.setNumeroFormula(formula.getNumeroFormula());
            despacho.setFechaGeneracionFormula(formula.getFechaEmision());
            despacho.setMedicoNombre(formula.getMedico() == null ? null
                    : nombreCompleto(formula.getMedico().getNombres(), formula.getMedico().getApellidos()));
        });
        if (despacho.getCitaId() != null) {
            citaRepository.findById(despacho.getCitaId()).ifPresent(cita -> {
                if (cita.getCliente() != null) {
                    despacho.setClienteId(cita.getCliente().getId());
                    despacho.setPropietarioNombre(nombreCompleto(
                            cita.getCliente().getNombres(), cita.getCliente().getApellidos()));
                }
                if (cita.getMascota() != null) {
                    despacho.setMascotaId(cita.getMascota().getMascotaId().longValue());
                    despacho.setMascotaNombre(cita.getMascota().getNombreMascota());
                }
            });
        }
    }

    private List<DespachoDetalle> enriquecerDetalles(List<DespachoDetalle> detalles) {
        for (DespachoDetalle detalle : detalles) {
            Despacho despacho = detalle.getDespacho();
            if (despacho != null && despacho.getNumeroFormula() == null) {
                completarDatosHistorial(despacho);
            }
            detalle.setMedicamentoNombre(detalle.getMedicamento() == null ? null
                    : detalle.getMedicamento().getNombre());
            detalle.setNumeroLote(detalle.getLote() == null ? null : detalle.getLote().getNumeroLote());
            detalle.setFechaVencimientoLote(detalle.getLote() == null
                    ? null : detalle.getLote().getFechaVencimiento());
            detalle.setEstadoDespacho(despacho == null ? null : despacho.getEstado());
            movimientoInventarioRepository.findByDespachoDetalle_Id(detalle.getId())
                    .ifPresent(movimiento -> {
                        detalle.setMovimientoInventarioId(movimiento.getId());
                        detalle.setMovimientoFecha(movimiento.getFechaMovimiento());
                        detalle.setMovimientoUsuarioId(movimiento.getUsuario() == null
                                ? null : movimiento.getUsuario().getId());
                        detalle.setMovimientoUsuarioNombre(movimiento.getUsuario() == null
                                ? null : movimiento.getUsuario().getUsername());
                        movimientoInventarioRepository.findFirstByMovimientoOriginal_Id(movimiento.getId())
                                .ifPresent(reversion -> {
                                    detalle.setMovimientoReversionId(reversion.getId());
                                    detalle.setMovimientoReversionFecha(reversion.getFechaMovimiento());
                                    detalle.setMovimientoReversionUsuarioId(reversion.getUsuario() == null
                                            ? null : reversion.getUsuario().getId());
                                    detalle.setMovimientoReversionUsuarioNombre(reversion.getUsuario() == null
                                            ? null : reversion.getUsuario().getUsername());
                                });
                    });
        }
        return detalles;
    }

    private void validarPrioridadFefo(List<FormulaDetalle> formulas,
                                      Map<Long, Long> cantidadPorDetalle,
                                      Map<Long, Long> cantidadPorLote,
                                      Map<Long, List<InventarioLote>> lotesPorMedicamento) {
        Map<Long, Long> cantidadPorMedicamento = new TreeMap<>();
        for (FormulaDetalle formula : formulas) {
            long cantidad = cantidadPorDetalle.getOrDefault(formula.getId(), 0L);
            if (cantidad > 0) {
                cantidadPorMedicamento.merge(formula.getMedicamentoId(), cantidad, Math::addExact);
            }
        }
        for (Map.Entry<Long, Long> entrada : cantidadPorMedicamento.entrySet()) {
            long restante = entrada.getValue();
            for (InventarioLote lote : lotesPorMedicamento.getOrDefault(entrada.getKey(), List.of())) {
                long cantidadEsperada = Math.min(restante, lote.getCantidad());
                long cantidadSeleccionada = cantidadPorLote.getOrDefault(lote.getId(), 0L);
                if (cantidadSeleccionada != cantidadEsperada) {
                    throw new BadRequestException("Seleccione primero los lotes vigentes con vencimiento más cercano.");
                }
                restante -= cantidadEsperada;
            }
            if (restante > 0) {
                throw new BadRequestException("No hay existencias vigentes suficientes para las cantidades seleccionadas.");
            }
        }
    }

    private boolean tieneCantidadesPendientes(List<FormulaDetalle> detalles) {
        return detalles.stream().anyMatch(detalle -> {
            validarDetalleFormula(detalle);
            return detalle.getCantidadPrescrita() > cantidadEntregadaFormula(detalle.getId());
        });
    }

    private long cantidadEntregadaFormula(Long formulaDetalleId) {
        return Optional.ofNullable(
                despachoDetalleRepository.sumarCantidadEntregadaPorFormulaDetalle(formulaDetalleId)).orElse(0L);
    }

    private boolean hayExistenciasElegibles(List<FormulaDetalle> formulas) {
        LocalDate hoy = LocalDate.now();
        for (FormulaDetalle formulaDetalle : formulas) {
            if (formulaDetalle.getCantidadPrescrita() <= cantidadEntregadaFormula(formulaDetalle.getId())) {
                continue;
            }
            List<InventarioLote> lotes = inventarioLoteRepository
                    .findByMedicamento_IdOrderByFechaVencimientoAsc(formulaDetalle.getMedicamentoId());
            if (lotes.stream().anyMatch(lote -> Boolean.TRUE.equals(lote.getActivo())
                    && lote.getCantidad() != null && lote.getCantidad() > 0
                    && !lote.getFechaVencimiento().isBefore(hoy))) {
                return true;
            }
        }
        return false;
    }

    private String validarClaveIdempotencia(String clave) {
        String normalizada = limpiar(clave);
        if (normalizada == null || !normalizada.matches("[A-Za-z0-9._:-]{1,100}")) {
            throw new BadRequestException("La clave de idempotencia es obligatoria y solo puede contener letras, números, punto, guion o dos puntos.");
        }
        return normalizada;
    }

    private String generarNumeroComprobante() {
        return "CP-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase(Locale.ROOT);
    }

    private String nombreCompleto(String nombres, String apellidos) {
        return ((nombres == null ? "" : nombres.trim()) + " " + (apellidos == null ? "" : apellidos.trim())).trim();
    }

    private boolean coincideFiltroHistorial(Despacho despacho, String filtro) {
        if (filtro == null) {
            return true;
        }
        String normalizado = Normalizer.normalize(filtro, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT);
        return contiene(despacho.getNumeroDespacho(), normalizado)
                || contiene(despacho.getNumeroFormula(), normalizado)
                || contiene(despacho.getPropietarioNombre(), normalizado)
                || contiene(despacho.getMascotaNombre(), normalizado)
                || (despacho.getFormulaId() != null && despacho.getFormulaId().toString().contains(normalizado));
    }

    private boolean contiene(String valor, String filtro) {
        if (valor == null) {
            return false;
        }
        String normalizado = Normalizer.normalize(valor, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT);
        return normalizado.contains(filtro);
    }

    private boolean coincideRangoFechas(Despacho despacho, LocalDate desde, LocalDate hasta) {
        if (desde == null && hasta == null) {
            return true;
        }
        if (despacho.getFechaDespacho() == null) {
            return false;
        }
        LocalDate fecha = despacho.getFechaDespacho().toLocalDate();
        return (desde == null || !fecha.isBefore(desde)) && (hasta == null || !fecha.isAfter(hasta));
    }

    private String limpiarTextoPdf(String texto) {
        if (texto == null) {
            return "";
        }
        return Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("[^\\x20-\\x7E]", " ")
                .trim();
    }

    private void bloquearCitaParaEntrega(Long formulaId) {
        Long citaId = formulaDetalleRepository.buscarCitaIdPorFormula(formulaId)
                .orElseThrow(() -> new BadRequestException("La fórmula no existe."));
        validarCitaParaEntrega(citaRepository.bloquearEstado(citaId)
                .orElseThrow(() -> new BadRequestException("La cita de la fórmula no existe.")));
    }

    private void validarCitaParaEntrega(String estado) {
        if ("CANCELADA".equals(estado)) {
            throw new BadRequestException("No se pueden dispensar medicamentos de una cita cancelada.");
        }
    }

    private long bloquearFormulaVigente(Long formulaId) {
        return formulaDetalleRepository.buscarFormulaVigenteParaBloqueo(formulaId)
                .orElseThrow(() -> new BadRequestException("La fórmula no existe, está inactiva o no está vigente."));
    }

    private Map<Long, Medicamento> bloquearMedicamentos(List<FormulaDetalle> formulas) {
        TreeSet<Long> ids = new TreeSet<>();
        for (FormulaDetalle formula : formulas) {
            validarDetalleFormula(formula);
            ids.add(formula.getMedicamentoId());
        }

        Map<Long, Medicamento> medicamentos = new TreeMap<>();
        for (Long id : ids) {
            Medicamento medicamento = medicamentoRepository.findLockedById(id)
                    .orElseThrow(() -> new BadRequestException("Un medicamento de la fórmula no existe."));
            medicamentos.put(id, medicamento);
        }
        return medicamentos;
    }

    private Map<Long, List<InventarioLote>> cargarLotesDisponibles(Iterable<Long> medicamentoIds) {
        Map<Long, List<InventarioLote>> lotes = new TreeMap<>();
        LocalDate hoy = LocalDate.now();
        for (Long medicamentoId : medicamentoIds) {
            lotes.put(medicamentoId,
                    inventarioLoteRepository.buscarDisponiblesPorVencimientoParaBloqueo(medicamentoId, hoy));
        }
        return lotes;
    }

    private void actualizarStockMedicamentos(
            Map<Long, Medicamento> medicamentos,
            Map<Long, Integer> cantidadesPorMedicamento) {
        for (Map.Entry<Long, Integer> entrada : cantidadesPorMedicamento.entrySet()) {
            Medicamento medicamento = medicamentos.get(entrada.getKey());
            int stockActual = medicamento.getCantidadActual() == null ? 0 : medicamento.getCantidadActual();
            if (stockActual < entrada.getValue()) {
                throw new BadRequestException("El stock general no coincide con los lotes disponibles del medicamento "
                        + medicamento.getNombre() + ".");
            }
            medicamento.setCantidadActual(stockActual - entrada.getValue());
            medicamentoRepository.save(medicamento);
        }
    }

    private void guardarMovimientoSalida(
            Despacho despacho,
            DespachoDetalle detalle,
            AsignacionLote asignacion,
            Usuario usuario,
            LocalDateTime fecha) {
        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setMedicamento(detalle.getMedicamento());
        movimiento.setLote(asignacion.lote);
        movimiento.setTipo("SALIDA");
        movimiento.setCantidad(asignacion.cantidad);
        movimiento.setFechaMovimiento(fecha);
        movimiento.setUsuario(usuario);
        movimiento.setFormula(formulaMedicaRepository.getReferenceById(despacho.getFormulaId()));
        movimiento.setDespacho(despacho);
        movimiento.setDespachoDetalle(detalle);
        movimiento.setObservacion("Salida por despacho " + despacho.getNumeroDespacho());
        movimientoInventarioRepository.save(movimiento);
    }

    private void revertirDetalle(
            Despacho despacho,
            DespachoDetalle detalle,
            Medicamento medicamento,
            Usuario usuario,
            String observacion) {
        MovimientoInventario movimientoOriginal = movimientoInventarioRepository
                .findByDespachoDetalle_Id(detalle.getId())
                .orElseThrow(() -> new BadRequestException(
                        "No se encontró el movimiento de inventario del detalle " + detalle.getId() + "."));
        Long loteId = detalle.getLote().getId();
        InventarioLote lote = inventarioLoteRepository.findLockedById(loteId)
                .orElseThrow(() -> new BadRequestException("El lote del despacho ya no existe."));

        lote.setCantidad(Math.addExact(lote.getCantidad(), detalle.getCantidadEntregada()));
        int stockActual = medicamento.getCantidadActual() == null ? 0 : medicamento.getCantidadActual();
        medicamento.setCantidadActual(Math.addExact(stockActual, detalle.getCantidadEntregada()));
        inventarioLoteRepository.save(lote);
        medicamentoRepository.save(medicamento);

        MovimientoInventario reversion = new MovimientoInventario();
        reversion.setMedicamento(medicamento);
        reversion.setLote(lote);
        reversion.setTipo("REVERSION");
        reversion.setCantidad(detalle.getCantidadEntregada());
        reversion.setFechaMovimiento(LocalDateTime.now());
        reversion.setUsuario(usuario);
        reversion.setFormula(formulaMedicaRepository.getReferenceById(despacho.getFormulaId()));
        reversion.setDespacho(despacho);
        reversion.setMovimientoOriginal(movimientoOriginal);
        reversion.setObservacion(observacion == null
                ? "Reversión por cancelación del despacho " + despacho.getNumeroDespacho()
                : observacion);
        movimientoInventarioRepository.save(reversion);
    }

    private void completarDatosCita(Despacho despacho, Long formulaId) {
        formulaDetalleRepository.buscarCitaIdPorFormula(formulaId).ifPresent(citaId -> {
            despacho.setCitaId(citaId);
            citaRepository.findById(citaId).ifPresent(cita -> {
                if (cita.getCliente() != null) {
                    despacho.setClienteId(cita.getCliente().getId());
                }
                if (cita.getMascota() != null && cita.getMascota().getMascotaId() != null) {
                    despacho.setMascotaId(cita.getMascota().getMascotaId().longValue());
                }
            });
        });
    }

    private Usuario validarUsuarioDespacho(String usuarioResponsableId) {
        Long usuarioId;
        try {
            usuarioId = Long.valueOf(usuarioResponsableId);
        } catch (RuntimeException exception) {
            throw new BadRequestException("La identidad autenticada no contiene un id de usuario válido.");
        }

        Usuario usuario = usuarioRepository.findOneById(usuarioId)
                .orElseThrow(() -> new BadRequestException("El usuario responsable no existe."));
        if (!Boolean.TRUE.equals(usuario.getActivo()) || usuario.getPerfil() == null
                || !Boolean.TRUE.equals(usuario.getPerfil().getActivo())) {
            throw new BadRequestException("El usuario responsable no está activo.");
        }
        String perfil = Normalizer.normalize(usuario.getPerfil().getNombre(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .trim()
                .toUpperCase(Locale.ROOT)
                .replace(' ', '_');
        if (!Set.of("DESPACHO_MEDICO", "ADMIN", "ADMINISTRADOR").contains(perfil)) {
            throw new BadRequestException("Solo los perfiles ADMIN y DESPACHO_MEDICO pueden operar despachos.");
        }
        return usuario;
    }

    private void validarDetalleFormula(FormulaDetalle formulaDetalle) {
        if (formulaDetalle == null || formulaDetalle.getId() == null
                || formulaDetalle.getMedicamentoId() == null || formulaDetalle.getMedicamentoId() <= 0
                || formulaDetalle.getCantidadPrescrita() == null || formulaDetalle.getCantidadPrescrita() <= 0) {
            throw new BadRequestException("La fórmula contiene un detalle inválido.");
        }
    }

    private Despacho obtenerDespacho(Long id) {
        validarId(id, "despacho");
        return despachoRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("El despacho no existe."));
    }

    private Long validarId(Long id, String entidad) {
        if (id == null || id <= 0) {
            throw new BadRequestException("El id del " + entidad + " debe ser mayor a cero.");
        }
        return id;
    }

    private String determinarEstado(List<FormulaDetalle> formulas,
                                    Map<Long, Long> pendientesAntes,
                                    Map<Long, Long> entregasActuales,
                                    Map<Long, List<InventarioLote>> lotesPorMedicamento,
                                    long cantidadEntregada) {
        Map<Long, Long> pendientePorMedicamento = new TreeMap<>();
        for (FormulaDetalle formula : formulas) {
            long pendienteFinal = pendientesAntes.getOrDefault(formula.getId(), 0L)
                    - entregasActuales.getOrDefault(formula.getId(), 0L);
            if (pendienteFinal > 0) {
                pendientePorMedicamento.merge(formula.getMedicamentoId(), pendienteFinal, Math::addExact);
            }
        }
        if (pendientePorMedicamento.isEmpty()) {
            return ESTADO_DESPACHADO;
        }
        if (cantidadEntregada == 0) {
            return ESTADO_PENDIENTE_DISPONIBILIDAD;
        }
        for (Map.Entry<Long, Long> entrada : pendientePorMedicamento.entrySet()) {
            long disponible = lotesPorMedicamento.getOrDefault(entrada.getKey(), List.of()).stream()
                    .mapToLong(lote -> lote.getCantidad() == null ? 0 : lote.getCantidad())
                    .sum();
            if (disponible < entrada.getValue()) {
                return ESTADO_PENDIENTE_DISPONIBILIDAD;
            }
        }
        return ESTADO_PARCIAL;
    }

    private String generarNumeroDespacho() {
        return "DS-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase(Locale.ROOT);
    }

    private void guardarHistorial(
            Despacho despacho,
            String estadoAnterior,
            String estadoNuevo,
            Usuario usuario,
            String observacion,
            LocalDateTime fechaCambio) {
        DespachoHistorialEstado historial = new DespachoHistorialEstado();
        historial.setDespacho(despacho);
        historial.setEstadoAnterior(estadoAnterior);
        historial.setEstadoNuevo(estadoNuevo);
        historial.setUsuario(usuario);
        historial.setObservacion(observacion);
        historial.setFechaCambio(fechaCambio);
        despachoHistorialEstadoRepository.save(historial);
    }

    private MiRespuestaRS respuesta(String mensaje) {
        MiRespuestaRS respuesta = new MiRespuestaRS();
        respuesta.setStatus(200);
        respuesta.setMessage(mensaje);
        return respuesta;
    }

    private String limpiar(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    private static final class AsignacionLote {
        private final FormulaDetalle formulaDetalle;
        private final InventarioLote lote;
        private final int cantidad;

        private AsignacionLote(FormulaDetalle formulaDetalle, InventarioLote lote, int cantidad) {
            this.formulaDetalle = formulaDetalle;
            this.lote = lote;
            this.cantidad = cantidad;
        }
    }
}
