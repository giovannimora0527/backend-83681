package com.uniminuto.veterinaria.serviceimpl;

import com.uniminuto.veterinaria.entity.FormulaMedica;
import com.uniminuto.veterinaria.repository.FormulaMedicaRepository;
import com.uniminuto.veterinaria.service.FormulaMedicaService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implementación de los servicios de negocio para la gestión de Fórmulas Médicas.
 */
@Service
public class FormulaMedicaServiceImpl implements FormulaMedicaService {

    private final FormulaMedicaRepository repository;

    public FormulaMedicaServiceImpl(FormulaMedicaRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<FormulaMedica> listarTodas() {
        return repository.findAllByOrderByFechaCreacionDesc();
    }

    @Override
    public FormulaMedica guardarFormula(FormulaMedica formulaMedica) {
        return repository.save(formulaMedica);
    }

    @Override
    public FormulaMedica actualizarFormula(Long id, FormulaMedica formulaMedica) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Fórmula médica no encontrada con ID: " + id);
        }
        formulaMedica.setId(id);
        return repository.save(formulaMedica);
    }
}