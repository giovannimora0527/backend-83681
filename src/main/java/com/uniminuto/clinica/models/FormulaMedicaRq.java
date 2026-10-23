package com.uniminuto.clinica.models;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Datos que envia el frontend para crear o actualizar una formula medica.
 */
@Data
public class FormulaMedicaRq {

    /**
     * Id de la formula. Solo se usa al actualizar.
     */
    private Long id;

    /**
     * Id del cliente dueño de la mascota.
     */
    private Long clienteId;

    /**
     * Id de la mascota.
     */
    private Long mascotaId;

    /**
     * Id de la cita (consulta) en la que se emite la formula.
     */
    private Long citaId;

    /**
     * Id del veterinario que emite la formula.
     */
    private Long medicoId;

    /**
     * Fecha de emision de la formula.
     */
    private LocalDateTime fechaEmision;

    /**
     * Nombre del medicamento.
     */
    private String medicamento;

    /**
     * Cantidad de la dosis (mayor que cero).
     */
    private BigDecimal dosisCantidad;

    /**
     * Unidad de la dosis.
     */
    private String dosisUnidad;

    /**
     * Frecuencia de administracion.
     */
    private String frecuencia;

    /**
     * Duracion del tratamiento en dias.
     */
    private Integer duracionDias;

    /**
     * Indicaciones para el cliente.
     */
    private String indicaciones;
}
