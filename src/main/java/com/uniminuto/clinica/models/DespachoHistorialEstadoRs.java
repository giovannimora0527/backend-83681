package com.uniminuto.clinica.models;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DespachoHistorialEstadoRs {

    private Long id;
    private Long despachoId;
    private String estadoAnterior;
    private String estadoNuevo;
    private Long usuarioId;
    private String observacion;
    private LocalDateTime fechaCambio;
}
