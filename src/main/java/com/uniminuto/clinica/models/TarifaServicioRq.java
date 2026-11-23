package com.uniminuto.clinica.models;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
@Data
public class TarifaServicioRq {
    private Long especializacionId;
    private Long medicoId;
    private String nombre;
    private BigDecimal valor;
    private BigDecimal impuestoPorcentaje;
    private LocalDate vigenciaDesde;
    private LocalDate vigenciaHasta;
}
