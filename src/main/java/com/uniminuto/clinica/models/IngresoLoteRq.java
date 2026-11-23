package com.uniminuto.clinica.models;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class IngresoLoteRq {

    private Long medicamentoId;
    private String codigoLote;
    private LocalDate fechaVencimiento;
    private Integer cantidad;
    private BigDecimal costoUnitario;
    private String detalle;
}
