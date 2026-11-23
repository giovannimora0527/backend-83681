package com.uniminuto.clinica.models;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Datos de entrada para registrar o actualizar una cita.
 */
@Data
public class CitaRq {

    private Long id;
<<<<<<< HEAD
    private Integer mascotaId;
    private Long medicoId;
    private LocalDateTime fechaHora;
    private String motivo;
    private String estado;
    private Integer duracionMinutos;
=======

    private Long clienteId;

    private Integer mascotaId;

    private Long medicoId;

    private LocalDateTime fechaHora;

    private String estado;

    private String motivo;
>>>>>>> 69d248a2c289f0849669bc921e5317721d7eb661
}
