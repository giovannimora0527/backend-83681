package com.uniminuto.clinica.models;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EntradaLoteRq(Long medicamentoId, String numeroLote, LocalDate fechaIngreso,
                            LocalDate fechaVencimiento, BigDecimal cantidad,
                            BigDecimal precioCompraUnitario, BigDecimal precioVentaUnitario,
                            String referenciaProveedor, Long usuarioId) { }
