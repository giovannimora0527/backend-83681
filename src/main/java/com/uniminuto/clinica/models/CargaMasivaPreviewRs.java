package com.uniminuto.clinica.models;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CargaMasivaPreviewRs {

    private String nombreArchivo;
    private int totalRegistros;
    private int registrosValidos;
    private int registrosConError;
    private List<MedicamentoRq> registrosValidosConfirmables = new ArrayList<>();
    private List<ErrorFilaDto> errores = new ArrayList<>();
}