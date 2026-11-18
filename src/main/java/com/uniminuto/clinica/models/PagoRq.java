package com.uniminuto.clinica.models;

import java.math.BigDecimal;

/** Monto, método y observación del pago asociado a una factura. */
public record PagoRq(BigDecimal monto, String metodo, String observacion) {
}
