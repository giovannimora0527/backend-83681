package com.uniminuto.clinica.models;

import lombok.Data;

<<<<<<< HEAD
import java.math.BigDecimal;

=======
import java.time.LocalDate;

/**
 * Datos de entrada para registrar o actualizar un medicamento.
 */
>>>>>>> 69d248a2c289f0849669bc921e5317721d7eb661
@Data
public class MedicamentoRq {

    private Long id;
<<<<<<< HEAD
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
=======

    private String nombre;

    private String descripcion;

    private String presentacion;

    private LocalDate fechaCompra;

    private LocalDate fechaVence;
>>>>>>> 69d248a2c289f0849669bc921e5317721d7eb661
}
