package com.uniminuto.clinica.models;
import lombok.Data;
@Data
public class DespacharFormulaRq {
    private Integer cantidad;
    private String claveIdempotencia;
}
