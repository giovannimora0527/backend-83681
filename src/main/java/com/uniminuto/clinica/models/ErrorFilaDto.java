package com.uniminuto.clinica.models;

import lombok.Data;

@Data
public class ErrorFilaDto {

    private int numeroFila;
    private String motivo;

    public ErrorFilaDto(int numeroFila, String motivo) {
        this.numeroFila = numeroFila;
        this.motivo = motivo;
    }
}