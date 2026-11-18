package com.uniminuto.clinica.models;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;

/** Solicitud para cancelar un despacho y devolver sus cantidades al inventario. */
@Schema(example = """
        {
          "observacion": "El propietario solicitó cancelar la entrega"
        }
        """)
@Data
public class DespachoCancelarRq {

    private String observacion;
}
