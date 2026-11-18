package com.uniminuto.clinica.models;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class MedicamentoRs {

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
    private List<LoteMedicamentoRs> lotes;
    private LocalDateTime fechaCreacionRegistro;
    private LocalDateTime fechaModificacionRegistro;
}