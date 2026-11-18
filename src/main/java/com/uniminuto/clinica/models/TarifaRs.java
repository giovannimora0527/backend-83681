package com.uniminuto.clinica.models;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Descripción y valores vigentes de una tarifa para consultarlos o seleccionarlos al finalizar la
 * atención.
 */
public record TarifaRs(long id, Long especializacionId, String nombreServicio, BigDecimal valorBase,
                      BigDecimal ivaPorcentaje, boolean activo, LocalDate fechaVigencia) {
}
