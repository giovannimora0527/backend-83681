package com.uniminuto.clinica.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Vista de una cita con los nombres resueltos para mostrar en el DataTable y en el detalle.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CitaVistaRS {

    private Long id;
    private Long clienteId;
    private String clienteNombre;
    private Long mascotaId;
    private String mascotaNombre;
    private Long medicoId;
    private String medicoNombre;
    private LocalDateTime fechaHora;
    private String motivo;
    private String estado;
}
