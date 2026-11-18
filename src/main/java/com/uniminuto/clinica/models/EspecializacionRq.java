package com.uniminuto.clinica.models;

/** Datos para crear o editar una especialidad, incluido su estado activo. */
public record EspecializacionRq(Long id, String nombre, String descripcion, String codigoEspecializacion,
                               Boolean activo) {
}
