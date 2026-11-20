package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.*;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.EntradaLoteRq;
import com.uniminuto.clinica.repository.*;
import com.uniminuto.clinica.service.InventarioService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class InventarioServiceImpl implements InventarioService {
    private final MedicamentoRepository medicamentos;
    private final InventarioLoteRepository lotes;
    private final InventarioMovimientoRepository movimientos;

    public InventarioServiceImpl(MedicamentoRepository medicamentos, InventarioLoteRepository lotes,
                                 InventarioMovimientoRepository movimientos) {
        this.medicamentos = medicamentos;
        this.lotes = lotes;
        this.movimientos = movimientos;
    }

    @Override @Transactional
    public InventarioLote registrarEntrada(EntradaLoteRq r) {
        validar(r);
        Medicamento medicamento = medicamentos.findById(r.medicamentoId())
                .orElseThrow(() -> new BadRequestException("El producto no existe."));
        InventarioLote lote = new InventarioLote();
        lote.setMedicamento(medicamento);
        lote.setNumeroLote(r.numeroLote().trim());
        lote.setFechaIngreso(r.fechaIngreso());
        lote.setFechaVencimiento(r.fechaVencimiento());
        lote.setCantidadInicial(r.cantidad());
        lote.setCantidadDisponible(r.cantidad());
        lote.setPrecioCompraUnitario(r.precioCompraUnitario());
        lote.setPrecioVentaUnitario(r.precioVentaUnitario());
        lote.setReferenciaProveedor(r.referenciaProveedor());
        lote.setCreadoPor(r.usuarioId());
        lote.setCreadoEn(LocalDateTime.now());
        InventarioLote guardado = lotes.save(lote);
        InventarioMovimiento movimiento = new InventarioMovimiento();
        movimiento.setLote(guardado);
        movimiento.setTipo("ENTRADA");
        movimiento.setCantidad(r.cantidad());
        movimiento.setReferenciaTipo("INGRESO");
        movimiento.setMotivo("Ingreso de lote al inventario");
        movimiento.setUsuarioId(r.usuarioId());
        movimiento.setCreadoEn(LocalDateTime.now());
        movimientos.save(movimiento);
        return guardado;
    }

    @Override public List<InventarioLote> consultarLotes(Long medicamentoId) {
        if (medicamentoId == null || !medicamentos.existsById(medicamentoId))
            throw new BadRequestException("El producto no existe.");
        return lotes.findByMedicamentoIdOrderByFechaVencimientoAsc(medicamentoId);
    }

    @Override public List<InventarioLote> consultarPorVencimiento(LocalDate hasta) {
        if (hasta == null || hasta.isBefore(LocalDate.now())) throw new BadRequestException("La fecha límite debe ser hoy o futura.");
        return lotes.findByFechaVencimientoBetweenOrderByFechaVencimientoAsc(LocalDate.now(), hasta);
    }

    @Override public List<InventarioMovimiento> movimientos(Long medicamentoId) {
        if (medicamentoId == null || !medicamentos.existsById(medicamentoId))
            throw new BadRequestException("El producto no existe.");
        return movimientos.findByLoteMedicamentoIdOrderByCreadoEnDesc(medicamentoId);
    }

    private static void validar(EntradaLoteRq r) {
        if (r == null || r.medicamentoId() == null || r.numeroLote() == null || r.numeroLote().isBlank()
                || r.fechaIngreso() == null || r.fechaVencimiento() == null || r.cantidad() == null
                || r.precioCompraUnitario() == null || r.precioVentaUnitario() == null)
            throw new BadRequestException("Producto, lote, fechas, cantidad y precios son obligatorios.");
        if (r.cantidad().compareTo(BigDecimal.ZERO) <= 0 || r.precioCompraUnitario().signum() < 0
                || r.precioVentaUnitario().signum() < 0)
            throw new BadRequestException("La cantidad debe ser positiva y los precios no pueden ser negativos.");
        if (r.fechaVencimiento().isBefore(r.fechaIngreso()))
            throw new BadRequestException("El vencimiento no puede ser anterior al ingreso.");
    }
}
