package com.uniminuto.clinica.models;

import lombok.Data;

/** Solicitud para despachar las cantidades pendientes de una fórmula vigente. */
@Data
public class DespachoRq {

    private Long formulaId;
    private String observacion;
}
