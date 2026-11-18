package com.uniminuto.clinica.models;

import lombok.Data;

/** Datos clínicos de una nota y sus referencias a la atención y al paciente. */
@Data
public class AnotacionHistoriaRq {
    private Long id;
    private Long citaId;
    private Long historiaId;
    private Long medicoId;
    private String descripcion;
    private String diagnostico;
    private String observaciones;
    private Long mascotaId;
}
