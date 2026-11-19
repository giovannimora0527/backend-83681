package com.uniminuto.clinica.models;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;

import java.time.LocalDateTime;

/** Contiene los datos enviados para registrar o editar una cuenta. */
@Schema(example = """
        {
          "username": "usuario.clinica",
          "password": "REEMPLAZA_CON_TU_CLAVE",
          "activo": true,
          "email": "usuario@example.test",
          "perfilId": 2
        }
        """)
@Data
public class UsuarioRq {
    /*
     * Identificador único del usuario.
     */
    private Long id;

    /**
     * Nombre de usuario.
     */
    private String username;

    /**
     * Contraseña a guardar.
     */
    private String password;

    /**
     * Rol del usuario.
     */
    private String rol;

    /**
     * Indica si el usuario está activo.
     */
    private Boolean activo;

    /**
     * Correo electrónico.
     */
    private String email;

    /**
     * Perfil Id del usuario.
     */
    private Integer perfilId;
}
