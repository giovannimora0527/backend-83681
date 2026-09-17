package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.FormulaMedica;
import com.uniminuto.clinica.exception.BadRequestException;

import java.util.List;

/**
 * Interfaz de servicio para la entidad Fórmula Médica.
 * Define el contrato de las operaciones y la lógica de negocio que se pueden
 * realizar sobre las fórmulas médicas del sistema.
 */
public interface FormulaMedicaService {

    /**
     * Obtiene una lista de todas las fórmulas médicas registradas,
     * organizadas según el criterio de ordenamiento definido en la implementación.
     *
     * @return Lista de entidades FormulaMedica.
     * @throws BadRequestException Si ocurre un error de validación o procesamiento al consultar los datos.
     */
    List<FormulaMedica> obtenerFormulasOrdenadas()
            throws BadRequestException;

}
