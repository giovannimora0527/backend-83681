package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.entity.Despacho;
import com.uniminuto.clinica.entity.DespachoDetalle;
import com.uniminuto.clinica.entity.DespachoHistorialEstado;
import com.uniminuto.clinica.entity.FormulaDetalle;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;

/** Implementa el despacho transaccional y la actualización de existencias por lote. */
@Service
public class DespachoServiceImpl implements DespachoService {

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
    public List<Despacho> listarDespachos() {
        return despachoRepository.findAllByOrderByIdDesc();
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public Despacho consultarDespacho(Long despachoId) {
        return obtenerDespacho(despachoId);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public List<Despacho> listarDespachosPorFormula(Long formulaId) {
        validarId(formulaId, "fórmula");
        return despachoRepository.findByFormulaIdOrderByIdDesc(formulaId);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public List<DespachoDetalle> listarDetalles(Long despachoId) {
        obtenerDespacho(despachoId);
        return despachoDetalleRepository.findByDespacho_IdOrderByIdAsc(despachoId);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public List<DespachoHistorialEstado> listarHistorial(Long despachoId) {
        obtenerDespacho(despachoId);
        return despachoHistorialEstadoRepository.findByDespacho_IdOrderByFechaCambioAsc(despachoId);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public Despacho guardarDespacho(DespachoRq despachoRq, String usuarioResponsableId) {
        if (despachoRq == null) {
            throw new BadRequestException("La información del despacho es obligatoria.");
        }
        validarId(despachoRq.getFormulaId(), "fórmula");
        Usuario usuario = validarUsuarioDespacho(usuarioResponsableId);
        Long formulaId = bloquearFormulaVigente(despachoRq.getFormulaId());

        List<FormulaDetalle> formulas = formulaDetalleRepository.findByFormulaIdOrderByIdAsc(formulaId);
        if (formulas.isEmpty()) {
            throw new BadRequestException("La fórmula no tiene medicamentos formulados para despachar.");
        }

        Map<Long, Medicamento> medicamentos = bloquearMedicamentos(formulas);
        Map<Long, List<InventarioLote>> lotesDisponibles = cargarLotesDisponibles(medicamentos.keySet());
        List<AsignacionLote> asignaciones = new ArrayList<>();
        Map<Long, Integer> cantidadesPorMedicamento = new TreeMap<>();
        long cantidadPendienteTotal = 0;
        long cantidadEntregadaTotal = 0;

        for (FormulaDetalle formulaDetalle : formulas) {
            validarDetalleFormula(formulaDetalle);
            long cantidadYaEntregada = Optional.ofNullable(
                    despachoDetalleRepository.sumarCantidadEntregadaPorFormulaDetalle(formulaDetalle.getId()))
                    .orElse(0L);
            long cantidadPendiente = formulaDetalle.getCantidadPrescrita() - cantidadYaEntregada;
            if (cantidadPendiente < 0) {
                throw new BadRequestException("La cantidad despachada supera la cantidad de la fórmula.");
            }
            if (cantidadPendiente == 0) {
                continue;
            }

            cantidadPendienteTotal += cantidadPendiente;
            long cantidadEntregada = asignarLotes(
                    formulaDetalle,
                    cantidadPendiente,
                    lotesDisponibles.get(formulaDetalle.getMedicamentoId()),
                    asignaciones);
            cantidadEntregadaTotal += cantidadEntregada;
            if (cantidadEntregada > 0) {
                cantidadesPorMedicamento.merge(
                        formulaDetalle.getMedicamentoId(), Math.toIntExact(cantidadEntregada), Math::addExact);
            }
        }

        if (cantidadPendienteTotal == 0) {
            throw new BadRequestException("La fórmula ya fue despachada completamente.");
        }

        actualizarStockMedicamentos(medicamentos, cantidadesPorMedicamento);
        LocalDateTime ahora = LocalDateTime.now();
        String estado = determinarEstado(cantidadPendienteTotal, cantidadEntregadaTotal);

        Despacho despacho = new Despacho();
        despacho.setNumeroDespacho(generarNumeroDespacho());
        despacho.setFormulaId(formulaId);
        despacho.setUsuarioDespacho(usuario);
        despacho.setFechaDespacho(cantidadEntregadaTotal > 0 ? ahora : null);
        despacho.setEstado(estado);
        despacho.setObservacion(limpiar(despachoRq.getObservacion()));
        completarDatosCita(despacho, formulaId);
        despacho = despachoRepository.save(despacho);

        for (AsignacionLote asignacion : asignaciones) {
            InventarioLote lote = asignacion.lote;
            inventarioLoteRepository.save(lote);

            DespachoDetalle detalle = new DespachoDetalle();
            detalle.setDespacho(despacho);
            detalle.setFormulaDetalleId(asignacion.formulaDetalle.getId());
            detalle.setMedicamento(medicamentos.get(asignacion.formulaDetalle.getMedicamentoId()));
            detalle.setLote(lote);
            detalle.setCantidadEntregada(asignacion.cantidad);
            detalle.setCantidadFormulada(asignacion.formulaDetalle.getCantidadPrescrita());
            detalle = despachoDetalleRepository.save(detalle);

            guardarMovimientoSalida(despacho, detalle, asignacion, usuario, ahora);
        }

        guardarHistorial(despacho, null, estado, usuario, despacho.getObservacion(), ahora);
        return despacho;
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

        String observacion = limpiar(cancelacionRq == null ? null : cancelacionRq.getObservacion());
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
        return respuesta("Despacho cancelado y cantidades devueltas al inventario.");
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

    private long asignarLotes(
            FormulaDetalle formulaDetalle,
            long cantidadPendiente,
            List<InventarioLote> lotes,
            List<AsignacionLote> asignaciones) {
        long restante = cantidadPendiente;
        if (lotes == null) {
            return 0;
        }
        for (InventarioLote lote : lotes) {
            if (restante == 0) {
                break;
            }
            int disponible = lote.getCantidad() == null ? 0 : lote.getCantidad();
            int asignado = (int) Math.min(restante, disponible);
            if (asignado > 0) {
                asignaciones.add(new AsignacionLote(formulaDetalle, lote, asignado));
                lote.setCantidad(disponible - asignado);
                restante -= asignado;
            }
        }
        return cantidadPendiente - restante;
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
        if (!"DESPACHO_MEDICO".equals(perfil)) {
            throw new BadRequestException("Solo el perfil DESPACHO_MEDICO puede registrar despachos.");
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

    private String determinarEstado(long cantidadPendiente, long cantidadEntregada) {
        if (cantidadEntregada == cantidadPendiente) {
            return ESTADO_DESPACHADO;
        }
        return cantidadEntregada > 0 ? ESTADO_PARCIAL : ESTADO_PENDIENTE_DISPONIBILIDAD;
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
