package com.uniminuto.clinica.models;

import lombok.Data;

@Data
public class AnotacionHistoriaRq {
    private Long historiaId;
    private Long medicoId;
    private String descripcion;
    private String observacion;
    private String diagnosticoEvolucion;
}