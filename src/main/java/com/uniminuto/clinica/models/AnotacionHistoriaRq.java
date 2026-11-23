package com.uniminuto.clinica.models;

import lombok.Data;

<<<<<<< HEAD
@Data
public class AnotacionHistoriaRq {
    private Long citaId;
    private Long historiaId;
    private Long medicoId;
    private String descripcion;
    private String diagnostico;
    private String observaciones;
    private Integer mascotaId;
=======
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
>>>>>>> 69d248a2c289f0849669bc921e5317721d7eb661
}
