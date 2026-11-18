package com.uniminuto.clinica.models;

import java.util.List;

/** Indicaciones generales y lista de medicamentos que el veterinario prescribe en una cita. */
public record PrescripcionRq(String indicaciones, List<MedicamentoPrescrito> medicamentos) {
    /** Datos de cada medicamento, incluida la vía de administración y la cantidad que se entregará. */
    public record MedicamentoPrescrito(Long medicamentoId, Integer cantidad, String dosis, String frecuencia,
                                       String duracion, String via, String indicaciones) {
    }
}
