package com.uniminuto.clinica.models;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ConfirmarCargaMasivaRq {

    private String nombreArchivo;
    private Long usuarioResponsableId;
    private List<MedicamentoRq> registros = new ArrayList<>();
}