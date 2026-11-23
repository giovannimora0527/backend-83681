package com.uniminuto.clinica.models;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
@Data
public class ReporteFacturacionRs {
    private LocalDate desde;
    private LocalDate hasta;
    private Integer totalFacturas;
    private BigDecimal subtotal;
    private BigDecimal descuentos;
    private BigDecimal impuestos;
    private BigDecimal total;
    private BigDecimal pagado;
    private BigDecimal saldo;
    private List<ReporteFacturaFilaRs> facturas;
}
