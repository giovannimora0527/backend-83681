# Facturación e informes

Las tarifas guardan precio, porcentaje de impuesto y vigencia. Al facturar una cita, el backend copia esos valores a un detalle inmutable; un cambio posterior en la tarifa no cambia facturas existentes. El descuento se registra como monto fijo y reduce la base gravable antes del impuesto.

## Flujo de facturación

1. Crear tarifa con `POST /clinica/v1/facturacion/tarifas`.
2. Emitir una sola factura para la cita con `POST /clinica/v1/facturacion/facturas`.
3. Registrar pagos parciales o totales en `POST /clinica/v1/facturacion/facturas/{id}/pagos`.
4. Anular con motivo mediante `POST /clinica/v1/facturacion/facturas/{id}/anular?motivo=...`.

Los pagos bloquean la factura durante el cálculo de saldo y no aceptan sobrepagos. La anulación conserva quién la hizo, cuándo y el motivo.

## Informes

Los informes aceptan `desde` y `hasta` con formato `AAAA-MM-DD`:

- Vista previa de citas: `/clinica/v1/reportes/citas?desde=2026-01-01&hasta=2026-12-31`.
- Citas Excel/PDF: `/clinica/v1/reportes/citas.xlsx` y `/clinica/v1/reportes/citas.pdf`.
- Vista previa financiera: `/clinica/v1/reportes/facturacion?desde=2026-01-01&hasta=2026-12-31`.
- Facturación Excel/PDF: `/clinica/v1/reportes/facturacion.xlsx` y `/clinica/v1/reportes/facturacion.pdf`.

Estos endpoints requieren JWT. Facturación es accesible por `ADMINISTRADOR` y `RECEPCIONISTA`; informes, por `ADMINISTRADOR`, `VETERINARIO` y `RECEPCIONISTA`.
