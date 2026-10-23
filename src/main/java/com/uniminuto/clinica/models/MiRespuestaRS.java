package com.uniminuto.clinica.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Respuesta genérica para las operaciones de escritura (crear, editar,
 * cambiar estado). Incluye el identificador del registro afectado.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MiRespuestaRS {

    /**
     * Código HTTP de la operación.
     */
    private int status;

    /**
     * Mensaje de la operación, entendible para el usuario.
     */
    private String message;

    /**
     * Identificador del registro creado o modificado.
     */
    private Long id;
}
