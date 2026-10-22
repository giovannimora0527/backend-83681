package com.uniminuto.clinica.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Vista de una anotación de historia clínica para el histórico paginado.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnotacionVistaRS {

    private Long id;
    private Long historiaId;
    private Long medicoId;
    private String medicoNombre;
    private LocalDateTime fechaHora;
    private String descripcion;
    private String observacion;
    private String diagnosticoEvolucion;
}
