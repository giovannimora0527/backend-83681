package com.uniminuto.clinica.models;

import lombok.Data;
import java.util.List;

@Data
public class ImportacionInventarioRs {
    private Long importacionId;
    private Integer totalFilas;
    private Integer filasValidas;
    private Integer filasImportadas;
    private Integer filasRechazadas;
    private List<FilaImportacionInventarioRs> filas;
}
