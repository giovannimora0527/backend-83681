package com.uniminuto.clinica.models;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Filtros del listado de fórmulas médicas.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class FormulaMedicaFiltroRq extends FiltroPaginadoRq {

    /**
     * true para ver solo las activas, false para las inactivas, vacío para todas.
     */
    private Boolean activo;
}
