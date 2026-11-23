# Estado de requisitos del parcial

Se implementaron las operaciones principales del backend y se conectó una pantalla de operación en el frontend `frontend26-83681`.

## Rúbrica

| Rubro | Peso | Estado | Entrega |
| --- | ---: | --- | --- |
| Inventario e importación masiva | 20 % | Implementado | Catálogo, lotes, movimientos, alertas, plantilla CSV, vista previa/importación CSV/XLSX (hasta 5.000 filas) y auditoría. La pantalla permite consultar e importar. |
| Agenda y citas | 20 % | Implementado | Horarios semanales, duración, disponibilidad por franjas, rechazo de profesionales inactivos y validación de cruces por médico y mascota. |
| Atención clínica, fórmulas y facturación | 20 % | Implementado | Cierre clínico, fórmulas con cantidad y vía sin afectar stock al crearlas, tarifas, factura con snapshot, pagos, anulación auditable y PDF individuales. |
| Informes | 10 % | Implementado | Consulta por rango y exportación PDF/XLSX de citas y facturación. |
| Despacho médico | 20 % | Implementado | Roles, bloqueo pesimista, FEFO, despacho parcial idempotente, cola y comprobante con trazabilidad. |
| Arquitectura e integridad | 5 % | Implementado | Migraciones Flyway V1-V8, validaciones, transacciones, JWT, OpenAPI y configuración Docker. |
| Presentación y documentación | 5 % | Implementado | Guías, plantilla CSV, referencia OpenAPI, pantalla integrada e instrucciones de configuración. |

La configuración para ejecutar el frontend en Docker está en [docker-frontend.md](docker-frontend.md).

## Verificación

- Backend: `docker compose build app` terminó en `BUILD SUCCESS`; el contenedor arrancó y Flyway aplicó V8.
- Frontend: `npx ng build datta-able-free-angular-admin-template --output-path <backend>/target/frontend-build` terminó correctamente tras los últimos ajustes. Se usó otra salida porque OneDrive bloqueó el `dist` original.
- No se ejecutaron pruebas automatizadas ni se validaron importaciones con archivos reales.
- Antes de la sustentación, ingresar datos demostrativos y capturar evidencia de cada flujo.

## Orden sugerido para demostrar

1. Iniciar servicios: `docker compose up -d`.
2. Configurar `BOOTSTRAP_ADMIN_USERNAME`, `BOOTSTRAP_ADMIN_EMAIL` y `BOOTSTRAP_ADMIN_PASSWORD` en `.env` si la base está nueva.
3. Crear catálogo y lotes, luego mostrar resumen/importación.
4. Configurar horarios, consultar disponibilidad y crear una cita dentro de una franja libre.
5. Cerrar la atención y crear la fórmula; mostrar que el stock solo cambia al despachar.
6. Mostrar el despacho parcial, su trazabilidad y su PDF/factura.
7. Exportar informes por rango de fechas.
