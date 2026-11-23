package com.uniminuto.clinica.models;
import lombok.Data;
import java.math.BigDecimal;
@Data
public class PagoFacturaRq {
    private BigDecimal monto;
    private String metodo;
    private String referencia;
}
