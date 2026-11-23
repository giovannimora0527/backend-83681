package com.uniminuto.clinica.util;

import com.uniminuto.clinica.exception.BadRequestException;

import java.time.LocalDate;

/**
 * Validaciones reutilizables de la capa de servicio. Los mensajes están pensados para el usuario final.
 */
public final class Validaciones {

    private Validaciones() {
    }

    /**
     * @param valor texto o referencia a validar.
     * @return true si el valor tiene contenido (no es nulo ni vacío).
     */
    public static boolean hayTexto(String valor) {
        return valor != null && !valor.isBlank();
    }

    /**
     * Verifica que un campo obligatorio esté diligenciado.
     *
     * @param valor  valor recibido (null o texto vacío no son válidos).
     * @param campo  nombre del campo para el mensaje.
     * @throws BadRequestException si el campo está vacío.
     */
    public static void requerido(Object valor, String campo) {
        boolean vacio = valor == null || (valor instanceof String s && s.isBlank());
        if (vacio) {
            throw new BadRequestException("Campo obligatorio sin diligenciar: " + campo);
        }
    }

    /**
     * Verifica que un id de referencia sea un número positivo.
     *
     * @param id    id recibido.
     * @param campo nombre del campo para el mensaje.
     * @throws BadRequestException si el id es nulo o no es positivo.
     */
    public static void idValido(Long id, String campo) {
        if (id == null || id <= 0) {
            throw new BadRequestException("Debe seleccionar un valor válido para: " + campo);
        }
    }

    /**
     * Igual que {@link #idValido(Long, String)}, para ids de tipo entero (por ejemplo, mascota_id).
     *
     * @param id    id recibido.
     * @param campo nombre del campo para el mensaje.
     * @throws BadRequestException si el id es nulo o no es positivo.
     */
    public static void idValido(Integer id, String campo) {
        if (id == null || id <= 0) {
            throw new BadRequestException("Debe seleccionar un valor válido para: " + campo);
        }
    }

    /**
     * Verifica la longitud máxima de un texto.
     *
     * @param valor texto a revisar (puede ser nulo).
     * @param max   longitud máxima permitida.
     * @param campo nombre del campo para el mensaje.
     * @throws BadRequestException si supera la longitud.
     */
    public static void maximo(String valor, int max, String campo) {
        if (valor != null && valor.trim().length() > max) {
            throw new BadRequestException("El campo " + campo + " no puede superar " + max + " caracteres");
        }
    }

    /**
     * Verifica que un rango de fechas de filtro sea coherente.
     *
     * @param desde fecha inicial (puede ser nula).
     * @param hasta fecha final (puede ser nula).
     * @throws BadRequestException si la inicial es posterior a la final.
     */
    public static void validarRango(LocalDate desde, LocalDate hasta) {
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new BadRequestException("La fecha inicial no puede ser posterior a la fecha final");
        }
    }

    /**
     * Construye el patrón LIKE para búsquedas de texto sin distinguir mayúsculas.
     *
     * @param texto texto buscado.
     * @return patrón en minúsculas con comodines.
     */
    public static String patron(String texto) {
        return "%" + texto.trim().toLowerCase() + "%";
    }
}
