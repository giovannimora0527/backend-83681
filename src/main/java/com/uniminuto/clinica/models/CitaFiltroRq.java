package com.uniminuto.clinica.models;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Filtros del listado de citas.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CitaFiltroRq extends FiltroPaginadoRq {

    /**
     * Estado de la cita a filtrar (PENDIENTE, CONFIRMADA...).
     */
    private String estado;
}
