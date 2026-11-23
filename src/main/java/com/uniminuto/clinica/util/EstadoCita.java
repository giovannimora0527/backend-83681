package com.uniminuto.clinica.util;

import com.uniminuto.clinica.exception.BadRequestException;

import java.util.Arrays;

/**
 * Estados válidos de una cita. En la base de datos se guarda la etiqueta (por ejemplo "Pendiente").
 */
public enum EstadoCita {

    PENDIENTE("Pendiente"),
    CONFIRMADA("Confirmada"),
    PROGRAMADA("Programada"),
    ATENDIDA("Atendida"),
    CANCELADA("Cancelada");

    /** Texto con el que se almacena y se muestra el estado. */
    private final String etiqueta;

    EstadoCita(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    /**
     * @return etiqueta del estado tal como se guarda en la base de datos.
     */
    public String getEtiqueta() {
        return etiqueta;
    }

    /**
     * Convierte un texto recibido (sin distinguir mayúsculas) en un estado válido.
     *
     * @param valor texto recibido desde el cliente.
     * @return estado correspondiente.
     * @throws BadRequestException si el texto está vacío o no corresponde a ningún estado.
     */
    public static EstadoCita desde(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new BadRequestException("El estado de la cita es obligatorio");
        }
        return Arrays.stream(values())
                .filter(e -> e.etiqueta.equalsIgnoreCase(valor.trim()))
                .findFirst()
                .orElseThrow(() -> new BadRequestException(
                        "Estado de cita no válido. Use: Pendiente, Confirmada, Programada, Atendida o Cancelada"));
    }
}
