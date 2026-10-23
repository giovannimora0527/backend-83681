package com.uniminuto.clinica.util;

import com.uniminuto.clinica.entity.Cliente;
import com.uniminuto.clinica.entity.Medico;

/**
 * Utilidades para construir los nombres que se muestran en los listados.
 */
public final class NombreUtil {

    private NombreUtil() {
        // Clase utilitaria: no se instancia.
    }

    /**
     * Nombre completo de un cliente.
     *
     * @param cliente cliente (puede ser null).
     * @return "nombres apellidos" o cadena vacía si no hay cliente.
     */
    public static String completo(Cliente cliente) {
        return cliente == null ? "" : unir(cliente.getNombres(), cliente.getApellidos());
    }

    /**
     * Nombre completo de un veterinario.
     *
     * @param medico veterinario (puede ser null).
     * @return "nombres apellidos" o cadena vacía si no hay veterinario.
     */
    public static String completo(Medico medico) {
        return medico == null ? "" : unir(medico.getNombres(), medico.getApellidos());
    }

    /**
     * Une dos textos con un espacio, ignorando los nulos.
     *
     * @param a primer texto.
     * @param b segundo texto.
     * @return texto unido y sin espacios sobrantes.
     */
    private static String unir(String a, String b) {
        return ((a == null ? "" : a) + " " + (b == null ? "" : b)).trim();
    }
}
