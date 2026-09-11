package com.uniminuto.clinica.models;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Representacion del usuario: No incluye el password.
 */
@Data
public class UsuarioRS {

    private Long usuarioId;

    private String username;

    private String email;

    private String rol;

    private LocalDateTime fechaCreacion;
}
