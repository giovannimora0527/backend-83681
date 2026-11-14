package com.uniminuto.clinica.models;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Clase que representa la solicitud para actualizar los datos de un lote.
 * La cantidad no se incluye porque sus cambios deben registrarse mediante movimientos.
 */
@Data
public class InventarioLoteActualizacionRq {

    /** Identificador del medicamento asociado al lote. */
    private Long medicamentoId;

    /** Número de lote informado por el proveedor. */
    private String numeroLote;

    /** Precio de compra unitario asociado al lote. */
    private BigDecimal precioCompraLote;

    /** Fecha de vencimiento del lote. */
    private LocalDate fechaVencimiento;

    /** Estado del lote; si no se informa, conserva su estado actual. */
    private Boolean activo;
}
