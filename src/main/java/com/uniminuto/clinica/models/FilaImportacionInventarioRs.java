package com.uniminuto.clinica.models;

import lombok.Data;

@Data
public class FilaImportacionInventarioRs {
    private Integer fila;
    private String codigoProducto;
    private String codigoLote;
    private String estado;
    private String mensaje;
}
