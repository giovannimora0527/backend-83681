package com.uniminuto.clinica.models;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Datos de entrada para crear o actualizar una historia clínica.
 * En la actualización solo se modifican motivo, diagnóstico y observaciones.
 */
@Data
public class HistoriaMedicaRq {

    private Long id;

    private Long clienteId;

    private Integer mascotaId;

    private Long citaId;

    private Long medicoId;

    private LocalDateTime fechaHora;

    private String motivo;

    private String diagnostico;

    private String observaciones;
}
