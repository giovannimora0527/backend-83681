package com.uniminuto.clinica.models;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Filtros del listado de historias clínicas.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class HistoriaMedicaFiltroRq extends FiltroPaginadoRq {

    /**
     * Estado de la historia a filtrar (ABIERTA o CERRADA).
     */
    private String estado;
}
