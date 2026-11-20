package com.uniminuto.clinica.models;
import java.math.BigDecimal;
public record TarifaServicioRq(Long especializacionId, String nombre, String descripcion, BigDecimal precio,
                               BigDecimal ivaPorcentaje, Boolean activo) { }
