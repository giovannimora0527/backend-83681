package com.uniminuto.clinica.models;

import lombok.Data;

import java.time.LocalDate;

@Data
public class InventarioItemRs {

    private Long medicamentoId;
    private String codigo;
    private String nombre;
    private String tipoProducto;
    private String presentacion;
    private String registroInvima;
    private Integer existenciaTotal;
    private Integer existenciaVencida;
    private Integer existenciaDispensable;
    private Integer stockMinimo;
    private String estadoStock;
    private Boolean proximoAVencer;
    private LocalDate vencimientoMasCercano;
}
