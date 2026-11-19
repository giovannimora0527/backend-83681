package com.uniminuto.clinica.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/** Reúne los datos que se imprimen al descargar una factura o prefactura. */
public record FacturaDocumento(Map<String, String> clinica, Map<String, Object> cabecera,
                               List<Linea> lineas, BigDecimal pagado, BigDecimal saldo, LocalDateTime generado) {

    /** Representa un servicio o producto con los importes e impuestos almacenados. */
    public record Linea(String tipo, String descripcion, int cantidad, BigDecimal precioUnitario,
                        BigDecimal subtotal, BigDecimal porcentajeIva, BigDecimal iva) {
    }
}
