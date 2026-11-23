package com.uniminuto.clinica.models;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class LoteInventarioRs {

    private Long id;
    private Long medicamentoId;
    private String codigoLote;
    private LocalDateTime fechaIngreso;
    private LocalDate fechaVencimiento;
    private Integer cantidadInicial;
    private Integer cantidadDisponible;
    private BigDecimal costoUnitario;
    private String estado;
}
