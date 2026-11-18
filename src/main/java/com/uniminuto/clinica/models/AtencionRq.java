package com.uniminuto.clinica.models;

import java.util.List;

/**
 * Datos clínicos y servicios del cierre. Se puede enviar una tarifa individual o una lista de
 * tarifas distintas.
 */
public record AtencionRq(Long tarifaId, String motivo, String sintomas, String diagnostico,
                         String observaciones, String recomendaciones, List<Long> tarifaIds) {

    public AtencionRq(Long tarifaId, String motivo, String sintomas, String diagnostico,
                      String observaciones, String recomendaciones) {
        this(tarifaId, motivo, sintomas, diagnostico, observaciones, recomendaciones, null);
    }
}
