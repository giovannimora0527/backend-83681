package com.uniminuto.clinica.models;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Datos del formulario individual de prescripción, incluida la vía de administración y la cantidad
 * prescrita.
 */
@Schema(example = """
        {
          "citaId": 9,
          "medicamentoId": 7,
          "cantidadPrescrita": 2,
          "dosis": "Según valoración veterinaria",
          "frecuencia": "Según pauta veterinaria",
          "duracion": "Hasta el control",
          "viaAdministracion": "Oral"
        }
        """)
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
