package com.uniminuto.clinica.models;

import lombok.Data;

@Data
public class CitaRq {

    private Long citaId;
    private Long mascotaId;
    private Long medicoId;
    private String fechaHora;
    private String motivo;
}
