package com.uniminuto.clinica.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Vista de una historia clínica con nombres resueltos. El listado usa los campos principales;
 * el detalle (modal "Ver") agrega diagnóstico y observaciones.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistoriaVistaRS {

    private Long id;
    private Long clienteId;
    private String clienteNombre;
    private Long mascotaId;
    private String mascotaNombre;
    private Long citaId;
    private String estadoCita;
    private Long medicoId;
    private String medicoNombre;
    private LocalDateTime fechaHora;
    private String motivo;
    private String diagnostico;
    private String observaciones;
    private LocalDateTime fechaCreacion;
}
