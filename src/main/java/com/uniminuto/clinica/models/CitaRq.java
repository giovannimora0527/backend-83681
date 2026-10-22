package com.uniminuto.clinica.models;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Datos de entrada para registrar o actualizar una cita.
 */
@Data
public class CitaRq {

    private Long id;

    private Long clienteId;

    private Integer mascotaId;

    private Long medicoId;

    private LocalDateTime fechaHora;

    private String estado;

    private String motivo;
}
