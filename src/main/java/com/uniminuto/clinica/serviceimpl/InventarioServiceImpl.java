package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.LoteMedicamento;
import com.uniminuto.clinica.entity.Medicamento;
import com.uniminuto.clinica.entity.MovimientoInventario;
import com.uniminuto.clinica.entity.TipoMovimientoInventario;
import com.uniminuto.clinica.entity.Usuario;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.IngresoLoteRq;
import com.uniminuto.clinica.models.InventarioItemRs;
import com.uniminuto.clinica.models.LoteInventarioRs;
import com.uniminuto.clinica.models.MovimientoInventarioRs;
import com.uniminuto.clinica.models.DespachoPendienteRs;
import com.uniminuto.clinica.models.ComprobanteDespachoRs;
import com.uniminuto.clinica.entity.FormulaMedica;
import com.uniminuto.clinica.repository.LoteMedicamentoRepository;
import com.uniminuto.clinica.repository.MedicamentoRepository;
import com.uniminuto.clinica.repository.MovimientoInventarioRepository;
import com.uniminuto.clinica.repository.UsuarioRepository;
import com.uniminuto.clinica.repository.FormulaMedicaRepository;
import com.uniminuto.clinica.service.InventarioService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class InventarioServiceImpl implements InventarioService {

    private static final int DIAS_ALERTA_VENCIMIENTO = 30;

    private final MedicamentoRepository medicamentoRepository;
    private final LoteMedicamentoRepository loteRepository;
    private final MovimientoInventarioRepository movimientoRepository;
    private final UsuarioRepository usuarioRepository;
    private final FormulaMedicaRepository formulaRepository;

    public InventarioServiceImpl(
            MedicamentoRepository medicamentoRepository,
            LoteMedicamentoRepository loteRepository,
            MovimientoInventarioRepository movimientoRepository,
            UsuarioRepository usuarioRepository,
            FormulaMedicaRepository formulaRepository) {
        this.medicamentoRepository = medicamentoRepository;
        this.loteRepository = loteRepository;
        this.movimientoRepository = movimientoRepository;
        this.usuarioRepository = usuarioRepository;
        this.formulaRepository = formulaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventarioItemRs> obtenerResumen(String estado) {
        List<Medicamento> medicamentos = medicamentoRepository.findAll().stream()
                .sorted((a, b) -> a.getNombre().compareToIgnoreCase(b.getNombre()))
                .toList();
        if (medicamentos.isEmpty()) {
            return List.of();
        }

        List<Long> ids = medicamentos.stream().map(Medicamento::getId).toList();
        Map<Long, List<LoteMedicamento>> lotesPorMedicamento = new HashMap<>();
        loteRepository.findByMedicamento_IdInOrderByFechaVencimientoAscIdAsc(ids)
                .forEach(lote -> lotesPorMedicamento
                        .computeIfAbsent(lote.getMedicamento().getId(), ignored -> new ArrayList<>())
                        .add(lote));

        LocalDate hoy = LocalDate.now();
        LocalDate limiteVencimiento = hoy.plusDays(DIAS_ALERTA_VENCIMIENTO);
        return medicamentos.stream()
                .map(medicamento -> construirResumen(
                        medicamento,
                        lotesPorMedicamento.getOrDefault(medicamento.getId(), List.of()),
                        hoy,
                        limiteVencimiento))
                .filter(item -> coincideEstado(item, estado))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LoteInventarioRs> obtenerLotes(Long medicamentoId) throws BadRequestException {
        validarMedicamentoExiste(medicamentoId);
        LocalDate hoy = LocalDate.now();
        return loteRepository.findByMedicamento_IdOrderByFechaVencimientoAscIdAsc(medicamentoId).stream()
                .map(lote -> convertirLote(lote, hoy))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovimientoInventarioRs> obtenerMovimientos(Long medicamentoId) throws BadRequestException {
        validarMedicamentoExiste(medicamentoId);
        return movimientoRepository.findByLote_Medicamento_IdOrderByFechaDescIdDesc(medicamentoId).stream()
                .map(this::convertirMovimiento)
                .toList();
    }

    @Override
    @Transactional
    public LoteInventarioRs registrarIngreso(IngresoLoteRq request) throws BadRequestException {
        validarIngreso(request);
        Medicamento medicamento = validarMedicamentoExiste(request.getMedicamentoId());
        if (!Boolean.TRUE.equals(medicamento.getActivo())) {
            throw new BadRequestException("No se puede ingresar stock a un producto inactivo.");
        }
        if (loteRepository.existsByMedicamento_IdAndCodigoLoteIgnoreCase(
                medicamento.getId(), request.getCodigoLote().trim())) {
            throw new BadRequestException("Ya existe ese código de lote para el producto.");
        }

        Usuario usuario = usuarioActual();
        LocalDateTime ahora = LocalDateTime.now();
        BigDecimal costo = request.getCostoUnitario().setScale(2, RoundingMode.HALF_UP);

        LoteMedicamento lote = new LoteMedicamento();
        lote.setMedicamento(medicamento);
        lote.setCodigoLote(request.getCodigoLote().trim());
        lote.setFechaIngreso(ahora);
        lote.setFechaVencimiento(request.getFechaVencimiento());
        lote.setCantidadInicial(request.getCantidad());
        lote.setCantidadDisponible(request.getCantidad());
        lote.setCostoUnitario(costo);
        lote.setUsuarioCreacion(usuario);
        lote.setFechaCreacion(ahora);
        lote = loteRepository.save(lote);

        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setLote(lote);
        movimiento.setTipo(TipoMovimientoInventario.INGRESO);
        movimiento.setCantidad(request.getCantidad());
        movimiento.setStockAnterior(0);
        movimiento.setStockNuevo(request.getCantidad());
        movimiento.setUsuario(usuario);
        movimiento.setDetalle(limpiar(request.getDetalle()));
        movimiento.setFecha(ahora);
        movimientoRepository.save(movimiento);

        return convertirLote(lote, LocalDate.now());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DespachoPendienteRs> obtenerDespachosPendientes() {
        return formulaRepository.findByEstadoDispensacionInOrderByFechaCreacionRegistroAsc(List.of("PENDIENTE", "PARCIAL")).stream().map(formula -> {
            DespachoPendienteRs row = new DespachoPendienteRs();
            row.setFormulaId(formula.getId()); row.setMedicamentoId(formula.getMedicamento().getId());
            row.setMedicamento(formula.getMedicamento().getNombre());
            row.setMascota(formula.getCita().getMascota().getNombreMascota());
            int despachado = movimientoRepository.findByFormula_IdOrderByFechaAscIdAsc(formula.getId()).stream()
                    .filter(m -> m.getTipo() == TipoMovimientoInventario.DESPACHO)
                    .mapToInt(MovimientoInventario::getCantidad).sum();
            row.setCantidad(Math.max(0, formula.getCantidadPrescrita() - despachado));
            row.setFechaCreacion(formula.getFechaCreacionRegistro());
            return row;
        }).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ComprobanteDespachoRs obtenerComprobanteDespacho(Long formulaId) {
        FormulaMedica formula = formulaRepository.findById(formulaId)
                .orElseThrow(() -> new BadRequestException("La fórmula indicada no existe."));
        if (!List.of("DESPACHADA", "PARCIAL").contains(formula.getEstadoDispensacion()))
            throw new BadRequestException("La fórmula todavía no ha sido despachada.");
        List<MovimientoInventario> moves = movimientoRepository.findByFormula_IdOrderByFechaAscIdAsc(formulaId);
        ComprobanteDespachoRs result = new ComprobanteDespachoRs(); result.setFormulaId(formulaId);
        result.setEstado(formula.getEstadoDispensacion()); result.setMedicamento(formula.getMedicamento().getNombre());
        result.setCantidadPrescrita(formula.getCantidadPrescrita());
        result.setCantidadDespachada(moves.stream().mapToInt(MovimientoInventario::getCantidad).sum());
        result.setFechaDespacho(moves.isEmpty() ? null : moves.get(0).getFecha());
        result.setLotes(moves.stream().map(this::convertirMovimiento).toList());
        return result;
    }

    private InventarioItemRs construirResumen(
            Medicamento medicamento,
            List<LoteMedicamento> lotes,
            LocalDate hoy,
            LocalDate limiteVencimiento) {
        int existenciaTotal = 0;
        int existenciaVencida = 0;
        int existenciaDispensable = 0;
        LocalDate vencimientoMasCercano = null;

        for (LoteMedicamento lote : lotes) {
            int cantidad = lote.getCantidadDisponible();
            existenciaTotal += cantidad;
            if (lote.getFechaVencimiento().isBefore(hoy)) {
                existenciaVencida += cantidad;
            } else {
                existenciaDispensable += cantidad;
                if (vencimientoMasCercano == null || lote.getFechaVencimiento().isBefore(vencimientoMasCercano)) {
                    vencimientoMasCercano = lote.getFechaVencimiento();
                }
            }
        }

        int stockMinimo = medicamento.getStockMinimo() == null ? 0 : medicamento.getStockMinimo();
        String estadoStock = !Boolean.TRUE.equals(medicamento.getActivo()) ? "INACTIVO"
                : existenciaDispensable == 0 ? "AGOTADO"
                : existenciaDispensable <= stockMinimo ? "BAJO" : "DISPONIBLE";
        InventarioItemRs response = new InventarioItemRs();
        response.setMedicamentoId(medicamento.getId());
        response.setCodigo(medicamento.getCodigo());
        response.setNombre(medicamento.getNombre());
        response.setTipoProducto(medicamento.getTipoProducto() == null ? null : medicamento.getTipoProducto().name());
        response.setPresentacion(medicamento.getPresentacion());
        response.setRegistroInvima(medicamento.getRegistroInvima());
        response.setExistenciaTotal(existenciaTotal);
        response.setExistenciaVencida(existenciaVencida);
        response.setExistenciaDispensable(existenciaDispensable);
        response.setStockMinimo(stockMinimo);
        response.setEstadoStock(estadoStock);
        response.setVencimientoMasCercano(vencimientoMasCercano);
        response.setProximoAVencer(vencimientoMasCercano != null
                && !vencimientoMasCercano.isAfter(limiteVencimiento));
        return response;
    }

    private boolean coincideEstado(InventarioItemRs item, String estado) {
        if (estado == null || estado.isBlank() || "TODOS".equalsIgnoreCase(estado.trim())) {
            return true;
        }
        String filtro = estado.trim().toUpperCase(Locale.ROOT);
        return switch (filtro) {
            case "POR_VENCER" -> Boolean.TRUE.equals(item.getProximoAVencer());
            case "BAJO", "AGOTADO", "DISPONIBLE", "INACTIVO" -> filtro.equals(item.getEstadoStock());
            default -> throw new BadRequestException("El estado debe ser TODOS, BAJO, AGOTADO, DISPONIBLE, INACTIVO o POR_VENCER.");
        };
    }

    private Medicamento validarMedicamentoExiste(Long medicamentoId) {
        if (medicamentoId == null || medicamentoId <= 0) {
            throw new BadRequestException("El identificador del medicamento debe ser mayor a cero.");
        }
        return medicamentoRepository.findById(medicamentoId)
                .orElseThrow(() -> new BadRequestException("El producto solicitado no existe."));
    }

    private void validarIngreso(IngresoLoteRq request) {
        if (request == null || request.getMedicamentoId() == null) {
            throw new BadRequestException("Debe indicar el producto para ingresar stock.");
        }
        if (request.getCodigoLote() == null || request.getCodigoLote().isBlank()
                || request.getCodigoLote().trim().length() > 80) {
            throw new BadRequestException("El código de lote es obligatorio y admite hasta 80 caracteres.");
        }
        if (request.getFechaVencimiento() == null || request.getFechaVencimiento().isBefore(LocalDate.now())) {
            throw new BadRequestException("No se puede ingresar un lote vencido o sin fecha de vencimiento.");
        }
        if (request.getCantidad() == null || request.getCantidad() <= 0) {
            throw new BadRequestException("La cantidad de ingreso debe ser mayor a cero.");
        }
        if (request.getCostoUnitario() == null || request.getCostoUnitario().signum() < 0) {
            throw new BadRequestException("El costo unitario es obligatorio y no puede ser negativo.");
        }
    }

    private Usuario usuarioActual() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BadRequestException("Se requiere una sesión autenticada para registrar movimientos.");
        }
        return usuarioRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new BadRequestException("No existe el usuario de la sesión actual."));
    }

    private LoteInventarioRs convertirLote(LoteMedicamento lote, LocalDate hoy) {
        LoteInventarioRs response = new LoteInventarioRs();
        response.setId(lote.getId());
        response.setMedicamentoId(lote.getMedicamento().getId());
        response.setCodigoLote(lote.getCodigoLote());
        response.setFechaIngreso(lote.getFechaIngreso());
        response.setFechaVencimiento(lote.getFechaVencimiento());
        response.setCantidadInicial(lote.getCantidadInicial());
        response.setCantidadDisponible(lote.getCantidadDisponible());
        response.setCostoUnitario(lote.getCostoUnitario());
        response.setEstado(lote.getFechaVencimiento().isBefore(hoy) ? "VENCIDO"
                : lote.getCantidadDisponible() == 0 ? "AGOTADO" : "DISPONIBLE");
        return response;
    }

    private MovimientoInventarioRs convertirMovimiento(MovimientoInventario movimiento) {
        MovimientoInventarioRs response = new MovimientoInventarioRs();
        response.setId(movimiento.getId());
        response.setLoteId(movimiento.getLote().getId());
        response.setCodigoLote(movimiento.getLote().getCodigoLote());
        response.setTipo(movimiento.getTipo().name());
        response.setCantidad(movimiento.getCantidad());
        response.setStockAnterior(movimiento.getStockAnterior());
        response.setStockNuevo(movimiento.getStockNuevo());
        response.setUsuario(movimiento.getUsuario().getUsername());
        response.setFormulaId(movimiento.getFormula() == null ? null : movimiento.getFormula().getId());
        response.setReferencia(movimiento.getReferencia());
        response.setDetalle(movimiento.getDetalle());
        response.setFecha(movimiento.getFecha());
        return response;
    }

    private String limpiar(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
