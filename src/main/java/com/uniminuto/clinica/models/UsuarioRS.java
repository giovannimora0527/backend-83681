package com.uniminuto.clinica.models;

import lombok.Data;

@Data
public class UsuarioRS {

    /**
     * Status de la peticion.
     */
    private int status;

    /**
     * Mensaje de la peticion.
     */
    private String message;
}
