package com.uniminuto.clinica.models;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO (Objeto de transferencia de datos) para la recepción de datos al crear o actualizar una cita.
 */

@Data
public class CitaRq {
    private Long id;
    private Integer clienteId;
    private Integer mascotaId;
    private Integer medicoId;
    private LocalDateTime fechaHora;
    private String estado;
    private String motivo;
}
