package com.uniminuto.clinica.models;

import lombok.Data;

import java.time.LocalDateTime;

@Data

public class CitaRs {

    /** Representación lógica de la entidad Cita con sus atributos
     *  y distribución a la base de datos
     *  */

    private Long id;

    private Integer clienteId;

    private Integer mascotaId;

    private Integer medicoId;

    private LocalDateTime fechaHora;

    private String estado;

    private String motivo;
}
