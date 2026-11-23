package com.uniminuto.clinica.models;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Data
public class PagoFacturaRs {
    private BigDecimal monto;
    private String metodo;
    private String referencia;
    private LocalDateTime fecha;
    private String usuario;
}
