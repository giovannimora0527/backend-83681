package com.uniminuto.clinica.entity;

/**
 * Estados de una historia clínica (consulta).
 */
public enum EstadoHistoria {

    /** La consulta está en curso y admite nuevas anotaciones. */
    ABIERTA,
    /** La consulta fue cerrada; ya no admite nuevas anotaciones. */
    CERRADA
}
