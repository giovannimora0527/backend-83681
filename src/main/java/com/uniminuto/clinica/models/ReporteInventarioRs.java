package com.uniminuto.clinica.models;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class ReporteInventarioRs {
    private LocalDate fechaCorte;
    private Integer productos;
    private Integer unidadesDisponibles;
    private Integer unidadesVencidas;
    private Integer productosBajoStock;
    private Integer productosProximosAVencer;
    private BigDecimal valorInventario;
    private List<Fila> productosDetalle;

    @Data
    public static class Fila {
        private Long medicamentoId;
        private String nombre;
        private String tipoProducto;
        private Integer existenciaTotal;
        private Integer existenciaDispensable;
        private Integer existenciaVencida;
        private Integer stockMinimo;
        private String estadoStock;
        private LocalDate vencimientoMasCercano;
        private BigDecimal valorExistencia;
    }
}
