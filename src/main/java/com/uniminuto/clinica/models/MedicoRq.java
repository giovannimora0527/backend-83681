package com.uniminuto.clinica.models;

import lombok.Data;

/**
 * Datos de entrada para registrar o actualizar un médico.
 */
@Data
public class MedicoRq {

    private Long id;

    private String tipoDocumento;

    private String numeroDocumento;

    private String nombres;

    private String apellidos;

    private String telefono;

    private String registroProfesional;

    private Long especializacionId;
}
