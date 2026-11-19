package com.uniminuto.clinica.models;

import lombok.Data;

/** Contiene los datos que se devuelven sobre una cantidad entregada de un medicamento y su lote. */
@Data
public class DespachoDetalleRs {

    private Long id;
    private Long despachoId;
    private Long formulaDetalleId;
    private Long medicamentoId;
    private Long loteId;
    private Integer cantidadEntregada;
    private Integer cantidadFormulada;
}
