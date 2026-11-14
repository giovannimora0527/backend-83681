package com.uniminuto.clinica.models;

import lombok.Data;

/** Solicitud para cancelar un despacho y devolver sus cantidades al inventario. */
@Data
public class DespachoCancelarRq {

    private String observacion;
}
