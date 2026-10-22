package com.uniminuto.clinica.util;

import com.uniminuto.clinica.exception.BadRequestException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Map;
import java.util.Set;

/**
 * Construye la paginación y el orden de los listados.
 * El orden solo se permite por campos en lista blanca para evitar consultas arbitrarias.
 */
public final class Paginacion {

    /** Máximo de registros por página. */
    public static final int TAMANIO_MAXIMO = 100;

    /**
     * Campos del cliente que apuntan a una propiedad de una relación de la entidad.
     * Por ejemplo, "medicamento" es la relación al catálogo; el orden real es por su nombre.
     */
    private static final Map<String, String> ALIAS_ORDEN = Map.of(
            "medicamento", "medicamento.nombre",
            "clienteId", "cliente.clienteId",
            "mascotaId", "mascota.mascotaId",
            "medicoId", "medico.id");

    private Paginacion() {
    }

    /**
     * Crea el Pageable del listado. Por defecto el orden es descendente (más recientes primero).
     *
     * @param pagina            número de página, desde 0.
     * @param tamanio           registros por página (1 a 100).
     * @param campo             campo por el que se ordena; si es nulo se usa {@code campoPorDefecto}.
     * @param direccion         "asc" o "desc"; cualquier otro valor se trata como descendente.
     * @param camposPermitidos  campos que el cliente puede usar para ordenar.
     * @param campoPorDefecto   campo de orden inicial.
     * @return configuración de paginación y orden.
     * @throws BadRequestException si la página, el tamaño o el campo no son válidos.
     */
    public static Pageable crear(int pagina, int tamanio, String campo, String direccion,
                                 Set<String> camposPermitidos, String campoPorDefecto) {
        if (pagina < 0) {
            throw new BadRequestException("El número de página no puede ser negativo");
        }
        if (tamanio < 1 || tamanio > TAMANIO_MAXIMO) {
            throw new BadRequestException("El tamaño de página debe estar entre 1 y " + TAMANIO_MAXIMO);
        }
        String campoFinal = (campo == null || campo.isBlank()) ? campoPorDefecto : campo.trim();
        if (!camposPermitidos.contains(campoFinal)) {
            throw new BadRequestException("No es posible ordenar por el campo: " + campoFinal);
        }
        Sort.Direction sentido = "asc".equalsIgnoreCase(direccion) ? Sort.Direction.ASC : Sort.Direction.DESC;
        String propiedad = ALIAS_ORDEN.getOrDefault(campoFinal, campoFinal);
        return PageRequest.of(pagina, tamanio, Sort.by(sentido, propiedad).and(Sort.by(Sort.Direction.DESC, "id")));
    }
}
