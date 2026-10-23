package com.uniminuto.clinica.models;

import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

/**
 * Respuesta paginada que consumen los DataTables del frontend.
 *
 * @param <T> tipo de los registros de la página.
 */
@Data
public class PaginaRs<T> {

    /**
     * Registros de la página actual.
     */
    private List<T> contenido;

    /**
     * Número de la página actual (empieza en 0).
     */
    private int pagina;

    /**
     * Cantidad de registros por página.
     */
    private int tamanio;

    /**
     * Total de registros que cumplen los filtros.
     */
    private long totalRegistros;

    /**
     * Total de páginas disponibles.
     */
    private int totalPaginas;

    /**
     * Construye la respuesta a partir de una página de Spring Data.
     *
     * @param page página consultada.
     * @param <T>  tipo de los registros.
     * @return respuesta paginada.
     */
    public static <T> PaginaRs<T> desde(Page<T> page) {
        return desde(page, Function.identity());
    }

    /**
     * Construye la respuesta a partir de una página de Spring Data,
     * transformando cada registro (por ejemplo, de entidad a DTO resumido).
     *
     * @param page      página consultada.
     * @param conversor función que transforma cada registro.
     * @param <E>       tipo original de los registros.
     * @param <T>       tipo final de los registros.
     * @return respuesta paginada con los registros transformados.
     */
    public static <E, T> PaginaRs<T> desde(Page<E> page, Function<E, T> conversor) {
        PaginaRs<T> rs = new PaginaRs<>();
        rs.setContenido(page.getContent().stream().map(conversor).toList());
        rs.setPagina(page.getNumber());
        rs.setTamanio(page.getSize());
        rs.setTotalRegistros(page.getTotalElements());
        rs.setTotalPaginas(page.getTotalPages());
        return rs;
    }
}
