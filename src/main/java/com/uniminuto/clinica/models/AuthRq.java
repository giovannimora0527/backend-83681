package com.uniminuto.clinica.models;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;

/** Contiene las credenciales enviadas para solicitar el inicio de sesión. */
@Schema(example = """
        {
          "username": "usuario.clinica",
          "password": "REEMPLAZA_CON_TU_CLAVE"
        }
        """)
@Data
public class AuthRq {
    private String username;
    private String password;
}
