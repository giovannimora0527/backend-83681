package com.uniminuto.clinica.models;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Clase que representa la solicitud para registrar un lote de inventario.
 */
@Schema(example = """
        {
          "medicamentoId": 7,
          "numeroLote": "LOT-AMOX-2027-001",
          "cantidad": 20,
          "precioCompraLote": 15000,
          "fechaVencimiento": "2028-01-15",
          "activo": true
        }
        """)
@Data
public class InventarioLoteRq {

    /** Identificador del medicamento asociado al lote. */
    private Long medicamentoId;

    /** Número de lote informado por el proveedor. */
    private String numeroLote;

    /** Cantidad inicial registrada para el lote. */
    private Integer cantidad;

    /** Precio de compra unitario asociado al lote. */
    private BigDecimal precioCompraLote;

    /** Fecha de vencimiento del lote. */
    private LocalDate fechaVencimiento;

    /** Estado inicial del lote; si no se informa, se activa. */
    private Boolean activo;
}
