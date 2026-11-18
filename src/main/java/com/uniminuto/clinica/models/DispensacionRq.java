package com.uniminuto.clinica.models;

import lombok.Data;

@Data
public class DispensacionRq {

    private Long formulaId;
    private Long medicamentoId;
    private Long usuarioId;
    private Integer cantidad;
    private Long loteId;
}