package com.uniminuto.clinica.models;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;

import java.time.LocalDate;
import java.math.BigDecimal;

/** Contiene los datos enviados para registrar o editar un producto del inventario. */
@Schema(example = """
        {
          "nombre": "Solución de cuidado veterinario",
          "descripcion": "Producto de cuidado externo",
          "presentacion": "Frasco 120 ml",
          "codigo": "SOLU0730",
          "tipoProducto": "MEDICAMENTO",
          "registroInvima": "ICA F9000 MV",
          "precioVenta": 22000,
          "precioCompra": 15000,
          "cantidadActual": 0,
          "stockMinimo": 5
        }
        """)
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
