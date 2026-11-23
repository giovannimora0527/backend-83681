package com.uniminuto.clinica.models;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MovimientoInventarioRs {

    private Long id;
    private Long loteId;
    private String codigoLote;
    private String tipo;
    private Integer cantidad;
    private Integer stockAnterior;
    private Integer stockNuevo;
    private String usuario;
    private Long formulaId;
    private String referencia;
    private String detalle;
    private LocalDateTime fecha;
}
