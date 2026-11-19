package com.uniminuto.clinica.models;

import lombok.Data;

/** Contiene los datos para agregar una anotación durante una atención. */
@Data
public class AnotacionRq {

    private Long mascotaId;

    private Long medicoId;

    private String descripcion;
}
