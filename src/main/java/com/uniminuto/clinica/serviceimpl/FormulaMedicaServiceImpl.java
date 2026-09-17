package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.FormulaMedica;
import com.uniminuto.clinica.repository.FormulaMedicaRepository;
import com.uniminuto.clinica.service.FormulaMedicaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implementacion de los servicios de negocio de la entidad FormulaMedica.
 */
@Service
public class FormulaMedicaServiceImpl implements FormulaMedicaService {

    /** Repositorio de acceso a datos de las formulas medicas. */
    @Autowired
    private FormulaMedicaRepository formulaMedicaRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<FormulaMedica> listarFormulasMedicas() {
        return this.formulaMedicaRepository.findAllByOrderByFechaCreacionDesc();
    }
}
