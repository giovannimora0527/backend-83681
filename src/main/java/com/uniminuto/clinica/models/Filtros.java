package com.uniminuto.clinica.models;

import java.time.LocalDate;

/**
 * Filtros de los listados paginados. Cada registro agrupa los filtros de un módulo.
 * Los valores nulos o vacíos se ignoran.
 */
public final class Filtros {

    private Filtros() {
    }

    /**
     * Filtros de citas.
     *
     * @param clienteId  cliente de la cita.
     * @param mascotaId  mascota de la cita.
     * @param medicoId   veterinario asignado.
     * @param estado     estado (Pendiente, Confirmada, ...).
     * @param fechaDesde fecha inicial (inclusive).
     * @param fechaHasta fecha final (inclusive).
     * @param texto      texto contenido en el motivo.
     */
    public record Cita(Long clienteId, Integer mascotaId, Long medicoId, String estado,
                       LocalDate fechaDesde, LocalDate fechaHasta, String texto) {
    }

    /**
     * Filtros de fórmulas médicas.
     *
     * @param mascotaId   mascota de la fórmula.
     * @param medicoId    veterinario que la emitió.
     * @param medicamento texto contenido en el nombre del medicamento.
     * @param fechaDesde  fecha de emisión inicial (inclusive).
     * @param fechaHasta  fecha de emisión final (inclusive).
     */
    public record Formula(Integer mascotaId, Long medicoId, String medicamento,
                          LocalDate fechaDesde, LocalDate fechaHasta) {
    }

    /**
     * Filtros de historias clínicas.
     *
     * @param clienteId  cliente.
     * @param mascotaId  mascota.
     * @param medicoId   veterinario.
     * @param fechaDesde fecha inicial (inclusive).
     * @param fechaHasta fecha final (inclusive).
     * @param texto      texto contenido en el motivo o el diagnóstico.
     */
    public record Historia(Long clienteId, Integer mascotaId, Long medicoId,
                           LocalDate fechaDesde, LocalDate fechaHasta, String texto) {
    }

    /**
     * Filtros de anotaciones de una historia.
     *
     * @param historiaId  historia clínica (obligatoria).
     * @param fechaDesde  fecha inicial (inclusive).
     * @param fechaHasta  fecha final (inclusive).
     * @param texto       texto contenido en la descripción.
     */
    public record Anotacion(Long historiaId, LocalDate fechaDesde, LocalDate fechaHasta, String texto) {
    }
}
