package com.uniminuto.clinica.models;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Clase que representa la información de un lote en las respuestas del API.
 */
@Data
public class InventarioLoteRs {

    /** Identificador del lote. */
    private Long id;

    /** Identificador del medicamento asociado. */
    private Long medicamentoId;

    /** Número de lote informado por el proveedor. */
    private String numeroLote;

    /** Cantidad disponible en el lote. */
    private Integer cantidad;

    /** Precio de compra unitario del lote. */
    private BigDecimal precioCompraLote;

    /** Fecha de vencimiento del lote. */
    private LocalDate fechaVencimiento;

    /** Fecha en la que se registró el lote. */
    private LocalDateTime fechaIngreso;

    /** Estado del lote. */
    private Boolean activo;
}
