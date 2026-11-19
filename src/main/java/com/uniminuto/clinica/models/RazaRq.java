package com.uniminuto.clinica.models;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;

/** Contiene los datos enviados para registrar o editar una raza. */
@Schema(example = """
        {
          "nombre": "Mestizo",
          "especie": "Canino"
        }
        """)
@Data
public class RazaRq {

    private Integer razaId;

    private String nombre;

    private String especie;
}
