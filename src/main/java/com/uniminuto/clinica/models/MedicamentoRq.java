package com.uniminuto.clinica.models;

import lombok.Data;

import java.time.LocalDate;
import java.math.BigDecimal;

@Data
public class MedicamentoRq {

    private Long id;
    private String nombre;
    private String descripcion;
    private String presentacion;
    private LocalDate fechaCompra;
    private LocalDate fechaVence;
    private String codigo;
    private String tipoProducto;
    private String registroInvima;
    private BigDecimal precioVenta;
    private BigDecimal precioCompra;
    private Integer cantidadActual;
    private Integer stockMinimo;
    private LocalDate fechaProximoPedido;
}
