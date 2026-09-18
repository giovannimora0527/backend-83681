package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.FormulaMedica;
import com.uniminuto.clinica.exception.BadRequestException;
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

    /**
     * Repositorio de fórmulas médicas.
     */
    @Autowired
    private FormulaMedicaRepository formulaMedicaRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<FormulaMedica> listarFormulasMedicas() throws BadRequestException {
        // Se listan las fórmulas médicas ordenadas de la más reciente a la más antigua.
        return this.formulaMedicaRepository.findAllByOrderByFechaCreacionDesc();
    }
}
