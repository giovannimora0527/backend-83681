package com.uniminuto.clinica.models;
import lombok.Data;
import java.time.LocalDateTime;
@Data
public class DespachoPendienteRs {
    private Long formulaId;
    private Long medicamentoId;
    private String medicamento;
    private String mascota;
    private Integer cantidad;
    private LocalDateTime fechaCreacion;
}
