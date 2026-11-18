package com.uniminuto.clinica.models;

import io.swagger.v3.oas.annotations.media.Schema;

/** Datos para crear o editar una especialidad, incluido su estado activo. */
@Schema(example = """
        {
          "nombre": "Medicina interna",
          "descripcion": "Atención de medicina interna veterinaria",
          "codigoEspecializacion": "MEDINT",
          "activo": true
        }
        """)
public record EspecializacionRq(Long id, String nombre, String descripcion, String codigoEspecializacion,
                               Boolean activo) {
}
