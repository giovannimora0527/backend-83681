package com.uniminuto.clinica.models;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Objeto de solicitud (request) usado para crear o actualizar una cita.
 */
@Data
public class CitaRq {


    private Long id;
    private Long clienteId;
    private Long mascotaId;
    private Long medicoId;
    private LocalDateTime fechaHora;
    private String estado;
    private String motivo;
}