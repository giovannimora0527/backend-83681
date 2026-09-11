package com.uniminuto.clinica.models;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Representacion publica del usuario: nunca incluye el password.
 */
@Data
public class UsuarioRS {

    private Long usuarioId;

    private String nombre;

    private String email;

    private String rol;

    private LocalDateTime fechaRegistro;

    private LocalDateTime fechaModificacion;
}
