package com.uniminuto.clinica.models;

import java.math.BigDecimal;

/** Importe del descuento y motivo de la autorización administrativa. */
public record DescuentoRq(BigDecimal monto, String motivo) {
}
