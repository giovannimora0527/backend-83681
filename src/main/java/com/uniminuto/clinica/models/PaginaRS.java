package com.uniminuto.clinica.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

/**
 * Respuesta paginada estándar para todos los listados DataTable.
 *
 * @param <T> tipo de cada registro del listado (normalmente una vista resumida).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaginaRS<T> {

    /** Registros de la página actual. */
    private List<T> contenido;

    /** Número de página actual, desde 0. */
    private int pagina;

    /** Registros solicitados por página. */
    private int tamanio;

    /** Total de registros que cumplen los filtros. */
    private long totalRegistros;

    /** Total de páginas disponibles. */
    private int totalPaginas;

    /**
     * Convierte una página de entidades en una página de vistas.
     *
     * @param page   página obtenida del repositorio.
     * @param mapper función que transforma cada entidad en su vista.
     * @param <E>    tipo de la entidad.
     * @param <T>    tipo de la vista.
     * @return página lista para enviar al cliente.
     */
    public static <E, T> PaginaRS<T> desde(Page<E> page, Function<E, T> mapper) {
        List<T> contenido = page.getContent().stream().map(mapper).toList();
        return new PaginaRS<>(contenido, page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages());
    }
}
