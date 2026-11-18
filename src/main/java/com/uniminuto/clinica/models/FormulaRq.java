package com.uniminuto.clinica.models;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Datos del formulario individual de prescripción, incluida la vía de administración y la cantidad
 * prescrita.
 */
@Data
public class FormulaRq {

      private Long id;

      private Long citaId;

      private Long medicamentoId;

      private String dosis;

      private String indicaciones;

      private LocalDateTime fechaCreacionRegistro;

      private LocalDateTime fechaModificacionRegistro;

      private String frecuencia;

      private String duracion;

      private String viaAdministracion;

      private Integer cantidadPrescrita;
}
