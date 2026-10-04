package com.uniminuto.clinica.models;

import lombok.Data;

@Data
public class FormulaMedicaRq {
    private Long id;
    private Long mascotaId;
    private Long medicoId;
    private String observaciones;
}
