package com.uniminuto.clinica.models;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Vista resumida de una fórmula médica para el listado (DataTable).
 * El detalle completo se obtiene con el servicio de consulta por id.
 */
@Data
public class FormulaMedicaResumenRs {

    /**
     * Identificador de la fórmula.
     */
    private Long formulaId;

    /**
     * Fecha de emisión.
     */
    private LocalDate fechaEmision;

    /**
     * Fecha de creación del registro (orden por defecto del listado).
     */
    private LocalDateTime fechaCreacion;

    /**
     * Nombre de la mascota.
     */
    private String mascota;

    /**
     * Nombre completo del cliente.
     */
    private String cliente;

    /**
     * Nombre completo del veterinario.
     */
    private String veterinario;

    /**
     * Medicamento formulado.
     */
    private String medicamento;

    /**
     * Posología resumida, por ejemplo "5 mg cada 8 h por 7 días".
     */
    private String posologia;

    /**
     * Indica si la fórmula está activa.
     */
    private Boolean activo;
}
