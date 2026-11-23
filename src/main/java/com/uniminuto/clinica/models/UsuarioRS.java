package com.uniminuto.clinica.models;

import jakarta.persistence.Column;
import lombok.Data;

<<<<<<< HEAD
import java.time.LocalDateTime;

=======
>>>>>>> 69d248a2c289f0849669bc921e5317721d7eb661
@Data
public class UsuarioRs {

<<<<<<< HEAD
    /*
     * Identificador único del usuario.
     */
    private Long id;

    /**
     * Nombre de usuario.
     */
    private String username;

    /**
     * Rol del usuario.
     */
    private String rol;

    /**
     * Fecha de creación del usuario.
     */
    private LocalDateTime fechaCreacion;

    /**
     * Indica si el usuario está activo.
     */
    private Boolean activo;

    /**
     * Correo electrónico.
     */
    private String email;
=======
    /**
     * Status de la peticion.
     */
    private int status;

    /**
     * Mensaje de la peticion.
     */
    private String message;
>>>>>>> 69d248a2c289f0849669bc921e5317721d7eb661
}
