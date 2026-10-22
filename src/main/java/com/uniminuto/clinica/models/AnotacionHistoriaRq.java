package com.uniminuto.clinica.models;

import lombok.Data;

/**
 * Datos de entrada para registrar o actualizar una anotación de historia clínica.
 */
@Data
public class AnotacionHistoriaRq {

    private Long id;

    private Long historiaId;

    private Long medicoId;

    private String descripcion;

    private String observacion;

    private String diagnosticoEvolucion;
}
