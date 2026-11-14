package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.MovimientoInventario;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.repository.MedicamentoRepository;
import com.uniminuto.clinica.repository.MovimientoInventarioRepository;
import com.uniminuto.clinica.service.MovimientoInventarioService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/** Implementa las consultas históricas de entradas y salidas. */
@Service
public class MovimientoInventarioServiceImpl implements MovimientoInventarioService {

    private final MovimientoInventarioRepository movimientoRepository;
    private final MedicamentoRepository medicamentoRepository;

    /** Construye el servicio con los repositorios de movimientos y medicamentos. */
    public MovimientoInventarioServiceImpl(MovimientoInventarioRepository movimientoRepository,
                                           MedicamentoRepository medicamentoRepository) {
        this.movimientoRepository = movimientoRepository;
        this.medicamentoRepository = medicamentoRepository;
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public List<MovimientoInventario> listarPorMedicamento(Long medicamentoId) {
        if (medicamentoId == null || medicamentoId <= 0) {
            throw new BadRequestException("El id del medicamento debe ser mayor a cero.");
        }
        if (!medicamentoRepository.existsById(medicamentoId)) {
            throw new BadRequestException("El medicamento no existe.");
        }
        return movimientoRepository.findByMedicamento_IdOrderByFechaMovimientoDescIdDesc(medicamentoId);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public List<MovimientoInventario> listarTodos() {
        return movimientoRepository.findAllByOrderByFechaMovimientoDescIdDesc();
    }
}
