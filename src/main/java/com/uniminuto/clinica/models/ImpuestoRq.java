package com.uniminuto.clinica.models;

import java.math.BigDecimal;

/** Porcentaje de IVA que se aplica a un medicamento al facturarlo. */
public record ImpuestoRq(BigDecimal ivaPorcentaje) {
}
