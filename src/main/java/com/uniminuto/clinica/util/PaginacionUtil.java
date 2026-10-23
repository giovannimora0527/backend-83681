package com.uniminuto.clinica.util;

import com.uniminuto.clinica.models.FiltroPaginadoRq;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Map;

/**
 * Utilidad para construir la paginación y el ordenamiento de los listados.
 * Solo se permite ordenar por las columnas que cada servicio declara
 * (lista blanca), así el usuario no puede ordenar por campos internos ni
 * provocar errores enviando nombres de columna inexistentes.
 */
public final class PaginacionUtil {

    /**
     * Tamaño máximo de página permitido.
     */
    public static final int TAMANIO_MAXIMO = 100;

    /**
     * Constructor privado: clase de utilidades estáticas.
     */
    private PaginacionUtil() {
    }

    /**
     * Construye el objeto de paginación de Spring Data a partir del filtro.
     *
     * @param filtro           filtro recibido (página, tamaño, columna y dirección).
     * @param columnasPermitidas mapa columna-del-frontend → propiedad de la entidad.
     * @param columnaDefecto   columna (llave del mapa) usada si no se envía ninguna.
     * @return paginación lista para usar en el repositorio. Si no se envía
     * dirección se ordena de forma descendente (lo más reciente primero).
     */
    public static Pageable construir(FiltroPaginadoRq filtro, Map<String, String> columnasPermitidas,
                                     String columnaDefecto) {
        int pagina = filtro.getPagina() == null || filtro.getPagina() < 0 ? 0 : filtro.getPagina();
        int tamanio = filtro.getTamanio() == null || filtro.getTamanio() < 1 ? 10
                : Math.min(filtro.getTamanio(), TAMANIO_MAXIMO);

        // Map.of no admite llaves nulas, por eso se valida el null antes de consultar el mapa.
        String columna = filtro.getOrdenarPor() != null && columnasPermitidas.containsKey(filtro.getOrdenarPor())
                ? filtro.getOrdenarPor() : columnaDefecto;
        Sort.Direction direccion = "asc".equalsIgnoreCase(filtro.getDireccion())
                ? Sort.Direction.ASC : Sort.Direction.DESC;

        return PageRequest.of(pagina, tamanio, Sort.by(direccion, columnasPermitidas.get(columna)));
    }
}
