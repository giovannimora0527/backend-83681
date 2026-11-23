package com.uniminuto.clinica.models;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class ReporteMedicosRs {
    private LocalDate desde;
    private LocalDate hasta;
    private Integer totalCitas;
    private Integer totalAtendidas;
    private Integer totalCanceladas;
    private List<Fila> medicos;

    @Data
    public static class Fila {
        private Long medicoId;
        private String medico;
        private Integer totalCitas;
        private Integer atendidas;
        private Integer canceladas;
        private Integer pendientes;
    }
}
