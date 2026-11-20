package com.uniminuto.clinica.models;
import java.math.BigDecimal;
import java.util.List;
public record ConfirmarDespachoRq(String idempotencyKey, List<Detalle> detalles) {
    public record Detalle(Long formulaDetalleId, BigDecimal cantidad) { }
}
