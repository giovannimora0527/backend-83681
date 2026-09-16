package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.FormulaMedica;
import com.uniminuto.clinica.repository.FormulaMedicaRepository;
import com.uniminuto.clinica.service.FormulaMedicaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implementación del servicio de fórmulas médicas.
 */
@Service
public class FormulaMedicaServiceImpl implements FormulaMedicaService {

    @Autowired
    private FormulaMedicaRepository formulaMedicaRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<FormulaMedica> obtenerFormulasOrdenadas() {
        return formulaMedicaRepository.findAllByOrderByFechaCreacionDesc();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FormulaMedica guardarFormula(FormulaMedica formulaMedica) {
        return formulaMedicaRepository.save(formulaMedica);
    }
}