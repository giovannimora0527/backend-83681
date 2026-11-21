package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.LoteMedicamento;
import com.uniminuto.clinica.entity.Medicamento;
import com.uniminuto.clinica.repository.InventarioMedicamentoBloqueoRepository;
import com.uniminuto.clinica.repository.LoteMedicamentoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class InventarioLoteService {
    public record NuevoLote(Long medicamentoId, String numeroLote, LocalDate fechaVence,
                            BigDecimal precioCompra, Integer cantidadInicial, LocalDate fechaIngreso) {}
    public record LoteRespuesta(Long id, Long medicamentoId, String numeroLote,
                               LocalDate fechaVence, BigDecimal precioCompra,
                               Integer cantidadInicial, Integer cantidadDisponible, LocalDate fechaIngreso) {}

    private final InventarioMedicamentoBloqueoRepository medicamentos;
    private final LoteMedicamentoRepository lotes;

    public InventarioLoteService(InventarioMedicamentoBloqueoRepository medicamentos,
                                LoteMedicamentoRepository lotes) {
        this.medicamentos = medicamentos;
        this.lotes = lotes;
    }

    @Transactional(readOnly = true)
    public List<LoteRespuesta> listar(Long medicamentoId) {
        if (!medicamentos.existsById(medicamentoId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Medicamento no encontrado");
        }
        return lotes.findByMedicamentoIdOrderByFechaVenceAscIdAsc(medicamentoId)
                .stream().map(this::convertir).toList();
    }

    @Transactional
    public LoteRespuesta registrar(NuevoLote datos) {
        if (datos == null || datos.medicamentoId() == null || datos.numeroLote() == null
                || datos.numeroLote().isBlank() || datos.numeroLote().length() > 50
                || datos.fechaVence() == null || datos.precioCompra() == null
                || datos.precioCompra().signum() < 0 || datos.precioCompra().scale() > 2
                || datos.precioCompra().precision() - datos.precioCompra().scale() > 10
                || datos.cantidadInicial() == null || datos.cantidadInicial() <= 0
                || datos.fechaIngreso() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Datos del lote invalidos");
        }
        if (datos.fechaVence().isBefore(datos.fechaIngreso())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El lote no puede vencer antes de su ingreso");
        }
        Medicamento medicamento = medicamentos.bloquear(datos.medicamentoId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Medicamento no encontrado"));
        String numero = datos.numeroLote().trim();
        if (lotes.existsByMedicamentoIdAndNumeroLote(medicamento.getId(), numero)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Numero de lote duplicado");
        }
        int actual = medicamento.getStockActual() == null ? 0 : medicamento.getStockActual();
        if (actual < 0 || actual > Integer.MAX_VALUE - datos.cantidadInicial()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Stock fuera del rango permitido");
        }
        LoteMedicamento lote = new LoteMedicamento();
        lote.setMedicamento(medicamento);
        lote.setNumeroLote(numero);
        lote.setFechaVence(datos.fechaVence());
        lote.setPrecioCompra(datos.precioCompra());
        lote.setCantidadInicial(datos.cantidadInicial());
        lote.setCantidadDisponible(datos.cantidadInicial());
        lote.setFechaIngreso(datos.fechaIngreso());
        LoteMedicamento guardado = lotes.saveAndFlush(lote);
        medicamento.setStockActual(actual + datos.cantidadInicial());
        medicamentos.save(medicamento);
        return convertir(guardado);
    }

    private LoteRespuesta convertir(LoteMedicamento lote) {
        return new LoteRespuesta(lote.getId(), lote.getMedicamento().getId(),
                lote.getNumeroLote(), lote.getFechaVence(), lote.getPrecioCompra(),
                lote.getCantidadInicial(), lote.getCantidadDisponible(), lote.getFechaIngreso());
    }
}
