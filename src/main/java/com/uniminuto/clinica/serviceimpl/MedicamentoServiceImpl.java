package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Medicamento;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.repository.MedicamentoRepository;
import com.uniminuto.clinica.service.MedicamentoService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MedicamentoServiceImpl implements MedicamentoService {

    private final MedicamentoRepository repository;

    public MedicamentoServiceImpl(MedicamentoRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Medicamento> listarMedicamentos() {
        return repository.findAll();
    }

    @Override
    public Medicamento guardarMedicamento(Medicamento medicamento) {
        validar(medicamento);
        medicamento.setId(null);
        return repository.save(medicamento);
    }

    @Override
    public Medicamento actualizarMedicamento(Medicamento medicamento) {
        validar(medicamento);
        if (medicamento.getId() == null || !repository.existsById(medicamento.getId())) {
            throw new BadRequestException("El medicamento que desea actualizar no existe");
        }
        return repository.save(medicamento);
    }

    private void validar(Medicamento medicamento) {
        if (medicamento == null || medicamento.getNombre() == null || medicamento.getNombre().isBlank()
                || medicamento.getFechaCompra() == null || medicamento.getFechaVence() == null) {
            throw new BadRequestException("Nombre, fecha de compra y fecha de vencimiento son obligatorios");
        }
        if (medicamento.getFechaVence().isBefore(medicamento.getFechaCompra())) {
            throw new BadRequestException("La fecha de vencimiento debe ser posterior a la fecha de compra");
        }
    }
}
