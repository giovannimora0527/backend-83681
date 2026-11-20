package com.uniminuto.clinica.models;
import java.math.BigDecimal;
public record FacturaRq(Long tarifaServicioId, BigDecimal descuento, String motivoDescuento) {}
