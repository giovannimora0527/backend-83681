package com.uniminuto.clinica.models;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;

@Schema(example = """
        {
          "tipoDocumento": "CC",
          "numeroDocumento": "9000000730",
          "nombres": "Valeria",
          "apellidos": "Rojas",
          "telefono": "3001200450",
          "registroProfesional": "MV-92810",
          "especializacionId": 16
        }
        """)
@Data
public class MedicoRq {

    private Long id;

    private String tipoDocumento;

    private String numeroDocumento;

    private String nombres;

    private String apellidos;

    private String telefono;

    private String registroProfesional;

    private Long especializacionId;
}
