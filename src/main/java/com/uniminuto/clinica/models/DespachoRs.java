package com.uniminuto.clinica.models;

import lombok.Data;

import java.time.LocalDateTime;

/** Resumen de un despacho. */
@Data
public class DespachoRs {

    private Long id;
    private String numeroDespacho;
    private String numeroComprobante;
    private String urlComprobantePdf;
    private LocalDateTime fechaGeneracionComprobante;
    private Long formulaId;
    private Long citaId;
    private Long clienteId;
    private Long mascotaId;
    private Long usuarioDespachoId;
    private LocalDateTime fechaDespacho;
    private String estado;
    private String observacion;
}
