package com.uniminuto.clinica.models;
import lombok.Data;
import java.math.BigDecimal;
@Data
public class FacturarRq {
    private Long citaId;
    private Long tarifaId;
    private Integer cantidad = 1;
    private BigDecimal descuento = BigDecimal.ZERO;
}
