package com.uniminuto.clinica.models;
import lombok.Data;
import java.time.LocalDateTime;
@Data
public class DespachoRs {
    private Long despachoId;
    private Long formulaId;
    private Integer cantidadSolicitada;
    private Integer cantidadDespachada;
    private Integer cantidadPendiente;
    private String estadoFormula;
    private String claveIdempotencia;
    private LocalDateTime fecha;
    private boolean repetido;
}
