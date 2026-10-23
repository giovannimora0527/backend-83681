# Segundo Parcial – Programación Web
**Clínica veterinaria: citas, historias clínicas, anotaciones y fórmulas médicas**
Backend: Spring Boot 4 + JPA + MySQL 8 · Frontend: Angular 19

---

## 1. Modelo de datos

```
CLIENTE (cliente_id)
 └── 1:N  MASCOTA (mascota_id, cliente_id FK)
           ├── 1:N  CITA (cita_id, mascota_id FK, medico_id FK, fecha_cita, estado, motivo)
           │          └── N:1  MEDICO (id)
           └── 1:N  HISTORIA_MEDICA (historia_id, mascota_id FK, cita_id FK UNIQUE, medico_id FK,
                     │                fecha_consulta, motivo, diagnostico, observaciones, estado)
                     └── 1:N  ANOTACION_HISTORIA (anotacion_id, historia_id FK, medico_id FK,
                                                  fecha_anotacion, descripcion, observacion, diagnostico)
FORMULA_MEDICA (formula_id, mascota_id FK, medico_id FK, cita_id FK, medicamento,
                dosis_cantidad, dosis_unidad, frecuencia, duracion_dias, indicaciones,
                fecha_emision, fecha_creacion, fecha_modificacion, activo)
```

- La cita **no** guarda el cliente: se obtiene por `cita.mascota.cliente` (evita incoherencias).
- `historia_medica.cita_id` es **UNIQUE**: una sola historia por cita.
- Todas las relaciones tienen **llave foránea**, por lo que no se puede crear información huérfana.
- Script de cambios: `database/parcial2_migracion.sql` (adapta y conserva los datos existentes).

## 2. Endpoints implementados
Base: `http://localhost:8080/clinica/v1`

| Método | Endpoint | Descripción |
|---|---|---|
| GET | `/cliente/all` | Listar clientes |
| GET | `/cliente/consultar?clienteId=` | Consultar un cliente |
| GET | `/mascota/por-cliente?clienteId=` | Listar mascotas de un cliente |
| GET | `/mascota/consultar?mascotaId=` | Consultar una mascota |
| GET | `/medico/all` | Listar veterinarios |
| GET | `/cita/listar` | Listar citas (fecha/hora desc) |
| GET | `/cita/consultar?citaId=` | Consultar una cita |
| GET | `/cita/por-mascota?mascotaId=` | Citas de una mascota |
| GET | `/cita/filtrar-by-fechas?fechaInicio=&fechaFinal=` | Citas en un rango |
| POST | `/cita/crear` | Crear cita (201) |
| PUT | `/cita/actualizar` | Editar cita |
| PATCH | `/cita/reactivar?citaId=` | Reactivar una cita cancelada |
| GET | `/historia-clinica/listar` | Listar historias (fecha desc) |
| GET | `/historia-clinica/consultar?historiaId=` | Consultar historia |
| POST | `/historia-clinica/crear` | Crear historia (201) |
| PATCH | `/historia-clinica/cerrar?historiaId=` | Cerrar historia |
| POST | `/anotacion-historia/crear` | Crear anotación (201) |
| GET | `/anotacion-historia/por-historia?historiaId=` | Anotaciones de una historia |
| GET | `/anotacion-historia/por-mascota?mascotaId=` | Historial de anotaciones de la mascota |
| GET | `/formula-medica/listar` | Listar fórmulas (fecha de creación desc) |
| GET | `/formula-medica/listar-ordenado?asc=` | Listar fórmulas asc/desc |
| GET | `/formula-medica/consultar?formulaId=` | Consultar fórmula |
| POST | `/formula-medica/crear` | Crear fórmula (201) |
| PUT | `/formula-medica/actualizar` | Editar fórmula |
| PATCH | `/formula-medica/inactivar?formulaId=` | Inactivar fórmula (no hay borrado físico) |

**Respuestas:** éxito → `{ status, message }`; error → `{ status, error, message }`.
**Códigos:** 200 OK · 201 Created · 400 datos inválidos · 404 no existe · 405 método no permitido · 409 conflicto con el estado actual · 500 error inesperado (mensaje genérico).

## 3. Principales reglas de negocio (todas validadas en el backend)

**Citas** (`CitaServiceImpl`)
1. Cliente, mascota y veterinario deben existir (404).
2. La mascota debe pertenecer al cliente (400).
3. Un veterinario no puede tener otra cita activa a menos de 15 minutos (409).
4. Una mascota no puede tener dos citas simultáneas (misma ventana de 15 min) (409).
5. No se registran citas en el pasado (400).
6. Estados: PENDIENTE, PROGRAMADA, CONFIRMADA, ATENDIDA, CANCELADA. Al crear solo los tres primeros.
7. CANCELADA no se edita; se reactiva con una operación explícita (`PATCH /reactivar`), que vuelve a validar la fecha y la disponibilidad.
8. ATENDIDA no se edita (es histórica); solo puede marcarse ATENDIDA una cita cuya fecha ya llegó.
9. Al editar no se cambia la mascota; si cambia la fecha o el veterinario, se revalida la disponibilidad.

**Historias clínicas y anotaciones** (`HistoriaClinicaServiceImpl`, `AnotacionHistoriaServiceImpl`)
- Relación cliente → mascota → cita validada; la cita debe estar ATENDIDA y sin historia previa (409).
- La fecha de la consulta no puede ser anterior a la cita ni futura.
- Solo una historia ABIERTA admite anotaciones; la fecha de la anotación la asigna el servidor.

**Fórmulas médicas** (`FormulaMedicaServiceImpl`)
1. La mascota existe, tiene cliente y pertenece al cliente elegido.
2. El veterinario existe; la cita existe, es de la misma mascota y está ATENDIDA.
3. La fecha de emisión no puede ser anterior a la consulta ni futura.
4. Dosis > 0, unidad válida (mg, g, ml, mg/kg, tableta, gotas, UI), duración de 1 a 365 días.
5. No hay borrado físico: se inactiva. Una fórmula inactiva no se edita.
6. Al editar no se puede cambiar la mascota, la cita ni el veterinario (origen clínico).

## 4. Decisiones técnicas relevantes
- **Arquitectura por capas** (api → controller → service → serviceimpl → repository → entity). Los controladores solo delegan; toda la lógica está en `serviceimpl`.
- **`ValidadorEntidades`** (`util`) centraliza las validaciones que se repiten (existencia y pertenencia cliente/mascota/cita/veterinario).
- **Excepciones con código HTTP**: `BadRequestException` (400) es la base; `NotFoundException` (404) y `ConflictException` (409) la extienden. `GlobalExceptionHandler` responde un JSON uniforme y oculta los detalles técnicos.
- **Detección de cruces de horario** con consultas JPQL (`@Query`) que excluyen las citas canceladas y la propia cita en edición.
- **Dosis y duración numéricas** (antes texto libre) para poder validar "valores válidos".
- **Inactivación lógica** (`activo`) en fórmulas, y estado ABIERTA/CERRADA en historias.
- **Frontend**: servicios por módulo para consumir la API; formularios template-driven (estrategia del curso) con validaciones; combos dependientes cliente → mascota → cita; clase reutilizable `TablaDatos` (filtro, orden y paginación) y componentes compartidos `app-paginador` y `app-tabla-anotaciones`; mensajes con SweetAlert y estados de carga.
