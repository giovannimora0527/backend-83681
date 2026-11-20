package com.uniminuto.clinica.models;
import java.math.BigDecimal;
public record FacturaPagoRq(BigDecimal monto, String medio, String referencia) {}
