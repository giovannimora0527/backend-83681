package com.uniminuto.clinica.util;

import com.uniminuto.clinica.exception.BadRequestException;

/**
 * Validaciones de datos de entrada reutilizadas por los servicios.
 * Todas lanzan {@link BadRequestException} con un mensaje para el usuario.
 */
public final class ValidacionUtil {

    /**
     * Constructor privado: clase de utilidades estáticas.
     */
    private ValidacionUtil() {
    }

    /**
     * Verifica que un valor obligatorio no sea nulo.
     *
     * @param valor  valor a validar.
     * @param campo  nombre del campo para el mensaje (por ejemplo "la mascota").
     * @throws BadRequestException si el valor es nulo.
     */
    public static void obligatorio(Object valor, String campo) throws BadRequestException {
        if (valor == null) {
            throw new BadRequestException("El campo " + campo + " es obligatorio.");
        }
    }

    /**
     * Verifica que un texto obligatorio tenga contenido y no supere la longitud máxima.
     *
     * @param valor    texto a validar.
     * @param campo    nombre del campo para el mensaje.
     * @param longitud longitud máxima permitida.
     * @return el texto sin espacios al inicio ni al final.
     * @throws BadRequestException si el texto está vacío o es demasiado largo.
     */
    public static String textoObligatorio(String valor, String campo, int longitud) throws BadRequestException {
        if (valor == null || valor.isBlank()) {
            throw new BadRequestException("El campo " + campo + " es obligatorio.");
        }
        return textoOpcional(valor, campo, longitud);
    }

    /**
     * Valida un texto opcional: si viene, no debe superar la longitud máxima.
     *
     * @param valor    texto a validar (puede ser nulo).
     * @param campo    nombre del campo para el mensaje.
     * @param longitud longitud máxima permitida.
     * @return el texto sin espacios extremos, o null si venía vacío.
     * @throws BadRequestException si el texto es demasiado largo.
     */
    public static String textoOpcional(String valor, String campo, int longitud) throws BadRequestException {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        String limpio = valor.trim();
        if (limpio.length() > longitud) {
            throw new BadRequestException("El campo " + campo + " no puede superar " + longitud + " caracteres.");
        }
        return limpio;
    }

    /**
     * Verifica que un número entero obligatorio esté dentro de un rango.
     *
     * @param valor  número a validar.
     * @param campo  nombre del campo para el mensaje.
     * @param minimo valor mínimo permitido (incluido).
     * @param maximo valor máximo permitido (incluido).
     * @throws BadRequestException si el número es nulo o está fuera del rango.
     */
    public static void enteroEnRango(Integer valor, String campo, int minimo, int maximo)
            throws BadRequestException {
        obligatorio(valor, campo);
        if (valor < minimo || valor > maximo) {
            throw new BadRequestException("El campo " + campo + " debe estar entre " + minimo + " y " + maximo + ".");
        }
    }

    /**
     * Construye el patrón LIKE para las búsquedas de texto libre.
     *
     * @param texto texto ingresado por el usuario.
     * @return patrón "%texto%" en minúsculas, o null si no hay texto.
     */
    public static String patronBusqueda(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        return "%" + texto.trim().toLowerCase() + "%";
    }
}
