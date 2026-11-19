package com.uniminuto.clinica.models;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;

/** Contiene el código de verificación enviado para completar el acceso. */
@Schema(example = """
        {
          "usuarioId": 4,
          "token": "123456",
          "username": "usuario.clinica"
        }
        """)
@Data
public class TokenRq {
    private Long usuarioId;
    private String token;
    private String username;
}
