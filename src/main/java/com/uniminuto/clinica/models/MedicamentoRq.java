package com.uniminuto.clinica.models;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MedicamentoRq {

    private Long id;
    private String codigo;
    private String tipoProducto;
    private String nombre;
    private String descripcion;
    private String presentacion;
    private String registroInvima;
    private BigDecimal precioCompra;
    private BigDecimal precioPublico;
    private Integer stockMinimo;
    private Boolean activo;
}
