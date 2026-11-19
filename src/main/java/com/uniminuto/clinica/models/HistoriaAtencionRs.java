package com.uniminuto.clinica.models;

import java.time.LocalDateTime;
import java.util.List;

/** Resumen de una historia clínica con sus datos de atención y notas de seguimiento. */
public record HistoriaAtencionRs(Long id, Long citaId, LocalDateTime fecha, String motivo, String sintomas,
                                  String diagnostico, String observaciones, String recomendaciones, String estado,
                                  List<Nota> anotaciones) {
    /** Contiene la información de una anotación que se muestra en el historial clínico. */
    public record Nota(Long id, Long medicoId, LocalDateTime fecha, String descripcion,
                       String diagnostico, String observaciones) {
    }
}
