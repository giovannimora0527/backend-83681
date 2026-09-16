package com.uniminuto.veterinaria.service;

import com.uniminuto.veterinaria.entity.FormulaMedica;
import com.uniminuto.veterinaria.repository.FormulaMedicaRepository;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * Servicio que contiene la logica de negocio para FormulaMedica.
 */
@Service
public class FormulaMedicaService {

    private final FormulaMedicaRepository repository;

    public FormulaMedicaService(FormulaMedicaRepository repository) {
        this.repository = repository;
    }

    /**
     * Lista todas las formulas medicas ordenadas por fecha reciente.
     * @return Lista de formulas medicas.
     */
    public List<FormulaMedica> listarTodas() {
        return repository.findAllByOrderByFechaCreacionDesc();
    }
}