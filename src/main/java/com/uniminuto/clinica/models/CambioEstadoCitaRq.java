package com.uniminuto.clinica.models;

import lombok.Data;

/**
 * Datos para cambiar el estado de una cita.
 */
@Data
public class CambioEstadoCitaRq {

    /**
     * Nuevo estado de la cita (PROGRAMADA, CONFIRMADA, ATENDIDA o CANCELADA).
     */
    private String estado;
}
