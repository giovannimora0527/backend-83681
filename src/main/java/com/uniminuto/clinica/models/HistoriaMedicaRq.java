package com.uniminuto.clinica.models;

import lombok.Data;

@Data
public class HistoriaMedicaRq {

    /**
     * Identificador del cliente propietario de la mascota.
     */
    private Long clienteId;

    /**
     * Identificador unico de la mascota.
     */
    private Long mascotaId;

    /**
     * Identificador unico de la cita.
     */
    private Long citaId;

    /**
     * Identificador unico del medico.
     */
    private Long medicoId;

    /**
     * Motivo de la consulta del cliente.
     */
    private String motivoConsulta;

    /**
     * Diagnóstico del médico.
     */
    private String diagnostico;

    /**
     * Observaciones del médico.
     */
    private String observaciones;
}