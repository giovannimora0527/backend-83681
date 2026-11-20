package com.uniminuto.clinica.models;
import java.math.BigDecimal;
import java.util.List;
public record AtencionRq(Long citaId, Long medicoId, String motivo, String sintomas,
                         String diagnostico, String observaciones, String recomendaciones,
                         List<Formula> formulas) {
    public record Formula(Long medicamentoId, BigDecimal cantidad, String dosis, String frecuencia,
                          String duracion, String viaAdministracion, String indicaciones) { }
}
