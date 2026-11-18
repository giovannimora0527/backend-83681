package com.uniminuto.clinica.models;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Título, fecha de generación, encabezados y filas que se usan para la consulta y la descarga del
 * reporte.
 */
public record ReporteRs(String titulo, LocalDateTime generado, List<String> columnas, List<List<Object>> filas) {
}
