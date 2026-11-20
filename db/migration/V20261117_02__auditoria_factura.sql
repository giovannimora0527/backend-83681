-- Complemento seguro de auditoría para facturación. Ejecutar una sola vez después de V20261117_01.
ALTER TABLE factura
  ADD COLUMN motivo_descuento VARCHAR(500) NULL,
  ADD COLUMN anulada_por BIGINT UNSIGNED NULL,
  ADD COLUMN descuento_autorizado_por BIGINT UNSIGNED NULL,
  ADD CONSTRAINT fk_factura_anulada_por FOREIGN KEY (anulada_por) REFERENCES usuario(id),
  ADD CONSTRAINT fk_factura_descuento_autorizado_por FOREIGN KEY (descuento_autorizado_por) REFERENCES usuario(id);



SELECT DATABASE() AS esquema_actual;

SELECT TABLE_NAME
FROM information_schema.tables
WHERE TABLE_SCHEMA = 'clinica'
  AND TABLE_NAME IN (
    'inventario_lote', 'inventario_movimiento', 'inventario_importacion',
    'cita_disponibilidad', 'cita_historial', 'atencion_medica',
    'formula_detalle', 'tarifa_servicio', 'factura', 'factura_detalle',
    'factura_pago', 'despacho', 'despacho_detalle', 'despacho_historial',
    'despacho_reversion', 'medico_usuario'
  )
ORDER BY TABLE_NAME;