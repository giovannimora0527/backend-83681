package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.FormulaMedica;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.repository.FormulaMedicaRepository;
import com.uniminuto.clinica.service.FormulaMedicaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FormulaMedicaServicelmpl implements FormulaMedicaService {

    @Autowired
    private FormulaMedicaRepository formulaMedicaRepository;


    @Override
    public List<FormulaMedica> obtenerFormulasOrdenadas()
    throws BadRequestException {
        List<FormulaMedica> formulas = formulaMedicaRepository.findAllByOrderByFechaCreacionRegistroDesc();

        // Validar que se hayan encontrado registros
        // Si la lista es null o está vacía, lanzar excepción de negocio
        if (formulas == null || formulas.isEmpty()) {
            throw new BadRequestException("No se encontraron fórmulas médicas.");
        }

        // Retornar la lista de fórmulas ordenadas
        return formulas;
    }
}
