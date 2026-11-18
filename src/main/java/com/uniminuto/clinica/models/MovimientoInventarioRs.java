package com.uniminuto.clinica.models;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MovimientoInventarioRs {

    private Long id;
    private Long medicamentoId;
    private Long loteId;
    private Integer cantidad;
    private String tipoMovimiento;
    private Long usuarioId;
    private Long formulaId;
    private LocalDateTime fechaMovimiento;
    private String detalle;
    private Integer stockAnterior;
    private Integer stockResultante;
}