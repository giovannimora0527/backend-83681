package com.uniminuto.clinica.models;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class FilaValidacionDto {

    private int numeroFila;
    private MedicamentoRq registro;
    private boolean valido;
    private List<String> errores = new ArrayList<>();
}