package com.uniminuto.clinica.models;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Data
public class ReporteFacturaFilaRs {
    private Long id;
    private Long citaId;
    private LocalDateTime fecha;
    private String estado;
    private BigDecimal subtotal;
    private BigDecimal descuento;
    private BigDecimal impuesto;
    private BigDecimal total;
    private BigDecimal pagado;
    private BigDecimal saldo;
}
