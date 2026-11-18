package com.uniminuto.clinica.models;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class MedicamentoRq {

    private Long id;
    private String nombre;
    private String descripcion;
    private String presentacion;
    private String codigoAlfanumerico;
    private Double precioCompra;
    private Double precioVenta;
    private Integer cantidadActual;
    private Integer stockMinimo;
    private LocalDate fechaIngresoInventario;
    private LocalDate fechaProximoPedido;
    private String registroInvima;
    private LocalDate fechaCompra;
    private LocalDate fechaVence;
    private List<LoteMedicamentoRq> lotes;
}