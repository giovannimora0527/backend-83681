package com.uniminuto.clinica.models;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;
@Data
public class ReporteCitasRs {
    private LocalDate desde;
    private LocalDate hasta;
    private Integer total;
    private List<ReporteCitaFilaRs> citas;
}
