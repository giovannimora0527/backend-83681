# Backend Clínica Veterinaria — Primer Parcial Programación Web (NRC 83861)

Este proyecto es el **base entregado por el docente** (Cliente, Mascota, Médico,
Raza, Especialización, Usuario) más las **3 partes nuevas** que resuelven los
5 requerimientos del parcial:

| Parte | Entidad(es)                         | Requerimiento cubierto                                  |
|-------|--------------------------------------|-----------------------------------------------------------|
| 1     | FormulaMedica                        | 1 — listar fórmulas médicas del inventario (orden desc)  |
| 2     | Cita                                  | 2/3 y 4 — filtrar citas por fecha + crear/actualizar      |
| 3     | HistoriaMedica + AnotacionHistoria   | 5 — CRUD historia médica (con filtro fecha) + CRUD anotación |

## Antes de ejecutar

1. Ajusta `src/main/resources/application-dev.properties` con tus credenciales de MySQL.
2. Ejecuta el script `sql/nuevas_tablas.sql` sobre la base de datos `clinica`
   (después de tener creadas cliente, mascota, medico, raza, especializacion, usuario).
3. Compila con `./mvnw clean package` o desde tu IDE.

## Endpoints nuevos

- `GET  /clinica/v1/formula-medica/listar-ordenado`
- `GET  /clinica/v1/cita/filtrar?fechaInicial=&fechaFinal=`
- `POST /clinica/v1/cita/guardar`
- `POST /clinica/v1/cita/actualizar`
- `POST /clinica/v1/historia-medica/guardar`
- `GET  /clinica/v1/historia-medica/listar?fechaInicial=&fechaFinal=`
- `POST /clinica/v1/historia-medica/actualizar`
- `POST /clinica/v1/anotacion-historia/guardar`
- `GET  /clinica/v1/anotacion-historia/listar?historiaId=`
- `POST /clinica/v1/anotacion-historia/actualizar`

(El `contextPath` configurado es `/clinica/v1`, y las fechas se envían en
formato ISO `yyyy-MM-ddTHH:mm:ss`, ej: `2026-01-15T09:30:00`).

## División del trabajo

El detalle de cada parte (entidad, endpoints, archivos y criterios de la
rúbrica) está en `Division_Parcial_3_Partes_NRC83861.pdf`, entregado junto
con este proyecto.
