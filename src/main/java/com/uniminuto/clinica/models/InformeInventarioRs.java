package com.uniminuto.clinica.models;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record InformeInventarioRs(LocalDateTime generado, String tipo, LocalDate desde, LocalDate hasta,
                                  String moneda, List<Seccion> secciones, Totales totales) {

    public record Seccion(String nombre, List<String> columnas, List<List<Object>> filas) {
    }

    public record Totales(long existenciasRegistradas, long disponiblesParaDispensar,
                          BigDecimal valorCosto, BigDecimal valorPotencialVenta, long entradas, long salidas) {
    }

    public List<List<Object>> parametros() {
        return List.of(
                List.of("Generado", generado),
                List.of("Tipo de informe", tipo),
                List.of("Desde (movimientos)", desde == null ? "Sin límite" : desde),
                List.of("Hasta (movimientos)", hasta == null ? "Sin límite" : hasta),
                List.of("Moneda", moneda),
                List.of("Corte de existencias y lotes", generado.toLocalDate()));
    }

    public List<List<Object>> filasTotales() {
        return List.of(
                List.of("Existencias registradas", totales.existenciasRegistradas()),
                List.of("Disponible para dispensar", totales.disponiblesParaDispensar()),
                List.of("Valor al costo (" + moneda + ")", totales.valorCosto()),
                List.of("Valor potencial de venta (" + moneda + ")", totales.valorPotencialVenta()),
                List.of("Unidades de entrada", totales.entradas()),
                List.of("Unidades de salida", totales.salidas()));
    }
}
