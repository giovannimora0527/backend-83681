package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.*;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.DispensacionRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.repository.*;
import com.uniminuto.clinica.service.DispensacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
public class DispensacionServiceImpl implements DispensacionService {

    @Autowired
    private FormulaMedicaRepository formulaMedicaRepository;

    @Autowired
    private MedicamentoRepository medicamentoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private LoteMedicamentoRepository loteMedicamentoRepository;

    @Autowired
    private MovimientoInventarioRepository movimientoInventarioRepository;

    @Override
    @Transactional
    public MiRespuestaRS dispensarMedicamento(DispensacionRq dispensacionRq) throws BadRequestException {
        validarSolicitud(dispensacionRq);

        FormulaMedica formula = formulaMedicaRepository.findById(dispensacionRq.getFormulaId())
                .orElseThrow(() -> new BadRequestException("La fórmula médica indicada no existe."));

        Medicamento medicamento = medicamentoRepository.findById(dispensacionRq.getMedicamentoId())
                .orElseThrow(() -> new BadRequestException("El medicamento indicado no existe en inventario."));

        Usuario usuario = usuarioRepository.findById(dispensacionRq.getUsuarioId())
                .orElseThrow(() -> new BadRequestException("El usuario responsable no existe."));

        if (!Objects.equals(formula.getMedicamento().getId(), medicamento.getId())) {
            throw new BadRequestException("La fórmula no corresponde al medicamento indicado.");
        }

        int cantidadSolicitada = dispensacionRq.getCantidad() == null ? formula.getCantidadSolicitada() == null ? 1 : formula.getCantidadSolicitada() : dispensacionRq.getCantidad();
        if (cantidadSolicitada <= 0) {
            throw new BadRequestException("La cantidad a dispensar debe ser mayor a cero.");
        }

        int cantidadDisponible = medicamento.getCantidadActual() == null ? 0 : medicamento.getCantidadActual();
        if (cantidadDisponible < cantidadSolicitada) {
            throw new BadRequestException("No hay suficiente inventario para dispensar la cantidad solicitada.");
        }

        List<LoteMedicamento> lotesOrdenados = medicamento.getLotes().stream()
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(LoteMedicamento::getFechaVencimientoLote))
                .toList();

        int cantidadRestante = cantidadSolicitada;
        for (LoteMedicamento lote : lotesOrdenados) {
            if (cantidadRestante <= 0) break;
            if (lote.getCantidad() == null || lote.getCantidad() <= 0) {
                continue;
            }

            int cantidadUsar = Math.min(cantidadRestante, lote.getCantidad());
            lote.setCantidad(lote.getCantidad() - cantidadUsar);
            cantidadRestante -= cantidadUsar;
        }

        if (cantidadRestante > 0) {
            throw new BadRequestException("No se pudo dispensar la cantidad solicitada porque los lotes seleccionados no cubren el inventario requerido.");
        }

        int stockAntes = medicamento.getCantidadActual();
        medicamento.setCantidadActual(medicamento.getCantidadActual() - cantidadSolicitada);
        if (medicamento.getCantidadActual() < 0) {
            throw new BadRequestException("La dispensación dejaría el inventario en valores negativos.");
        }

        LoteMedicamento loteSeleccionado = null;
        if (dispensacionRq.getLoteId() != null) {
            loteSeleccionado = loteMedicamentoRepository.findById(dispensacionRq.getLoteId())
                    .orElseThrow(() -> new BadRequestException("El lote indicado no existe."));
            if (!Objects.equals(loteSeleccionado.getMedicamento().getId(), medicamento.getId())) {
                throw new BadRequestException("El lote no pertenece al medicamento seleccionado.");
            }
        }

        formula.setCantidadSolicitada(cantidadSolicitada);
        formulaMedicaRepository.save(formula);
        medicamentoRepository.save(medicamento);

        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setFechaMovimiento(LocalDateTime.now());
        movimiento.setMedicamento(medicamento);
        movimiento.setLoteMedicamento(loteSeleccionado);
        movimiento.setCantidad(cantidadSolicitada);
        movimiento.setTipoMovimiento("SALIDA");
        movimiento.setUsuario(usuario);
        movimiento.setFormulaMedica(formula);
        movimiento.setDetalle("Dispensación de medicamento por fórmula médica");
        movimiento.setStockAnterior(stockAntes);
        movimiento.setStockResultante(medicamento.getCantidadActual());
        movimientoInventarioRepository.save(movimiento);

        MiRespuestaRS respuesta = new MiRespuestaRS();
        respuesta.setStatus(200);
        respuesta.setMessage("Dispensación realizada correctamente.");
        return respuesta;
    }

    @Override
    public List<MovimientoInventario> listarHistorialPorMedicamento(Long medicamentoId) throws BadRequestException {
        if (medicamentoId == null || medicamentoId <= 0) {
            throw new BadRequestException("El ID del medicamento es obligatorio.");
        }
        return movimientoInventarioRepository.findByMedicamentoIdOrderByFechaMovimientoDesc(medicamentoId);
    }

    @Override
    public List<MovimientoInventario> listarHistorialPorFormula(Long formulaId) throws BadRequestException {
        if (formulaId == null || formulaId <= 0) {
            throw new BadRequestException("El ID de la fórmula es obligatorio.");
        }
        return movimientoInventarioRepository.findByFormulaMedicaIdOrderByFechaMovimientoDesc(formulaId);
    }

    private void validarSolicitud(DispensacionRq dispensacionRq) throws BadRequestException {
        if (dispensacionRq == null) {
            throw new BadRequestException("La solicitud de dispensación es obligatoria.");
        }
        if (dispensacionRq.getFormulaId() == null || dispensacionRq.getFormulaId() <= 0) {
            throw new BadRequestException("El ID de la fórmula es obligatorio.");
        }
        if (dispensacionRq.getMedicamentoId() == null || dispensacionRq.getMedicamentoId() <= 0) {
            throw new BadRequestException("El ID del medicamento es obligatorio.");
        }
        if (dispensacionRq.getUsuarioId() == null || dispensacionRq.getUsuarioId() <= 0) {
            throw new BadRequestException("El ID del usuario responsable es obligatorio.");
        }
        if (dispensacionRq.getCantidad() != null && dispensacionRq.getCantidad() <= 0) {
            throw new BadRequestException("La cantidad debe ser mayor a cero.");
        }
    }
}
