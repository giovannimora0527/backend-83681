package com.uniminuto.clinica.models;
import lombok.Data;
import java.math.BigDecimal;
@Data
public class DetalleFacturaRs {
    private Long despachoId;
    private Long formulaId;
    private Long medicamentoId;
    private String despachadoPor;
    private String descripcion;
    private Integer cantidad;
    private BigDecimal valorUnitario;
    private BigDecimal impuestoPorcentaje;
    private BigDecimal descuento;
    private BigDecimal totalLinea;
}
