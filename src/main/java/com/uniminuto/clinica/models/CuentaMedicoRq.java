package com.uniminuto.clinica.models;

import io.swagger.v3.oas.annotations.media.Schema;

/** Identificador de la cuenta que se desea vincular con un veterinario. */
@Schema(example = """
        {
          "usuarioId": 4
        }
        """)
public record CuentaMedicoRq(Long usuarioId) {
}
