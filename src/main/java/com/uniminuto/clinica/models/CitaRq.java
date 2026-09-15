package com.uniminuto.clinica.models;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CitaRq {

    private Long id;

    private Long clienteId;

    private Long medicoId;

    private Long mascotaId;

    private LocalDateTime fechaHora;

    private String motivo;

    private String estado;
}
