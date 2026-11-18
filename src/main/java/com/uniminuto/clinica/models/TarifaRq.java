package com.uniminuto.clinica.models;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Datos del servicio que se va a cobrar, con precio, IVA, vigencia y especialidad cuando
 * corresponda.
 */
public record TarifaRq(Long especializacionId, String nombreServicio, BigDecimal valorBase,
                       BigDecimal ivaPorcentaje, Boolean activo, LocalDate fechaVigencia) {
}
