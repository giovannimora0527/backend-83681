# backend-83681
Backend para una clinica veterinaria

## Servicios del primer parcial

Context path: `http://localhost:8080/clinica/v1`

| # | Requerimiento | Metodo | Endpoint |
|---|---------------|--------|----------|
| 1 | Listar formulas medicas (mas reciente a mas antigua) | GET | `/formula-medica/listar` |
| 2-3 | Filtrar citas por rango de fechas (desc) | GET | `/cita/listar?fechaInicial=2026-01-01&fechaFinal=2026-12-31` |
| 4 | Crear cita | POST | `/cita/guardar` |
| 4 | Actualizar cita | POST | `/cita/actualizar` |
| 5 | Listar historias medicas con filtro de fechas (desc) | GET | `/historia-medica/listar?fechaInicial=2026-01-01&fechaFinal=2026-12-31` |
| 5 | Crear historia medica | POST | `/historia-medica/guardar` |
| 5 | Actualizar historia medica | POST | `/historia-medica/actualizar` |
| 5 | Listar anotaciones (opcional por historia) | GET | `/anotacion-historia/listar?historiaId=1` |
| 5 | Crear anotacion | POST | `/anotacion-historia/guardar` |
| 5 | Actualizar anotacion | POST | `/anotacion-historia/actualizar` |

El script de creacion de las tablas nuevas esta en `src/main/resources/db/script_parcial.sql`.
