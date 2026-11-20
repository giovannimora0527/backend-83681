package com.uniminuto.clinica.models;
import java.math.BigDecimal;
import java.time.LocalDate;
public record ProductoRq(String nombre, String descripcion, String presentacion, String codigo,
                         String tipoProducto, String registroInvima, BigDecimal precioCompra,
                         BigDecimal precioVenta, BigDecimal stockMinimo, LocalDate fechaProximoPedido,
                         Boolean activo) { }
