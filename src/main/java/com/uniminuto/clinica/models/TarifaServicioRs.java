package com.uniminuto.clinica.models;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class TarifaServicioRs {
    private Long id;
    private String nombre;
    private BigDecimal valor;
    private BigDecimal impuestoPorcentaje;
    private LocalDate vigenciaDesde;
    private LocalDate vigenciaHasta;
    private Boolean activo;
    private Long especializacionId;
    private String especializacionNombre;
    private Long medicoId;
    private String medicoNombre;
}
