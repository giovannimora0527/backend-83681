package com.uniminuto.clinica.models;

import lombok.Data;

@Data
public class HistoriaMedicaRq {

    private Long id;

    private Long citaId;

    private String diagnostico;

}
