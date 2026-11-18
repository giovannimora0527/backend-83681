package com.uniminuto.clinica.models;

import lombok.Data;

import java.time.LocalDate;

@Data
public class LoteMedicamentoRq {

    private Long id;
    private String numeroLote;
    private Integer cantidad;
    private Double precioCompraLote;
    private LocalDate fechaIngresoLote;
    private LocalDate fechaVencimientoLote;
}
