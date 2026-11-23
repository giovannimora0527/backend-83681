package com.uniminuto.clinica.models;

import lombok.Data;

<<<<<<< HEAD
=======
import java.time.LocalDateTime;

/**
 * Datos de entrada para crear o actualizar una historia clínica.
 * En la actualización solo se modifican motivo, diagnóstico y observaciones.
 */
>>>>>>> 69d248a2c289f0849669bc921e5317721d7eb661
@Data
public class HistoriaMedicaRq {

    private Long id;

<<<<<<< HEAD
    private Integer mascotaId;
=======
    private Long clienteId;

    private Integer mascotaId;

    private Long citaId;

    private Long medicoId;

    private LocalDateTime fechaHora;

    private String motivo;

    private String diagnostico;

    private String observaciones;
>>>>>>> 69d248a2c289f0849669bc921e5317721d7eb661
}
