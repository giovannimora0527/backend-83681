package com.uniminuto.clinica.models;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
@Data
public class ComprobanteDespachoRs {
    private Long formulaId;
    private String estado;
    private String medicamento;
    private Integer cantidadPrescrita;
    private Integer cantidadDespachada;
    private LocalDateTime fechaDespacho;
    private List<MovimientoInventarioRs> lotes;
}
