package com.uniminuto.clinica.models;

import lombok.Data;

@Data
public class MascotaRq {

    private Long mascotaId;

    private String nombreMascota;

    private Integer edad;

    private Integer razaId;

    private Long clienteId;
}
