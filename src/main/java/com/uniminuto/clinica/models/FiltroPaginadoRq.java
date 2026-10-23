package com.uniminuto.clinica.models;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * Parámetros comunes de los listados paginados. Se reciben como query params,
 * por ejemplo: {@code ?pagina=0&tamanio=10&ordenarPor=fecha&direccion=desc&texto=luna}.
 */
@Data
public class FiltroPaginadoRq {

    /**
     * Número de página solicitado (empieza en 0).
     */
    private Integer pagina = 0;

    /**
     * Cantidad de registros por página.
     */
    private Integer tamanio = 10;

    /**
     * Columna por la que se ordena. Cada servicio define qué columnas acepta.
     */
    private String ordenarPor;

    /**
     * Dirección del ordenamiento: "asc" o "desc".
     */
    private String direccion;

    /**
     * Texto libre para buscar en varias columnas a la vez.
     */
    private String texto;

    /**
     * Fecha inicial del rango a filtrar (incluida).
     */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaDesde;

    /**
     * Fecha final del rango a filtrar (incluida).
     */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaHasta;

    /**
     * Filtra por el veterinario indicado.
     */
    private Long medicoId;
}
