package com.uniminuto.clinica.models;
import lombok.Data;
import java.time.LocalDateTime;
@Data
public class ReporteCitaFilaRs {
    private Long id;
    private LocalDateTime fechaHora;
    private String cliente;
    private String mascota;
    private String medico;
    private String estado;
}
