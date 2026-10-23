# Segundo Parcial – Programación Web · Clínica Veterinaria

Estudiante: José Aguilar (904876) · Backend Spring Boot 4 + MySQL · Frontend Angular 19

## 1. Cómo ejecutar

1. Ejecutar `sql/clinica_parcial2.sql` en MySQL (**borra y recrea** la base `clinica` con datos de prueba).
   Las fechas de prueba se calculan desde la fecha actual, así siempre hay citas pasadas y futuras.
2. Ajustar usuario/clave en `src/main/resources/application-dev.properties` (por defecto `root` / `1234`).
3. Backend: `./mvnw spring-boot:run` (Java 21+). API en `http://localhost:8080/clinica/v1`.
4. Frontend (rama `904876_Jose_Aguilar` del repo de frontend): `npm install` y `npm start` → `http://localhost:4200`.
5. Pruebas: `./mvnw test` (no necesita MySQL: las pruebas de integración usan H2 en memoria).

## 2. Modelo de datos

```
CLIENTE 1 ── N MASCOTA 1 ── N CITA N ── 1 VETERINARIO (medico)
                   │           │ 1
                   │           └── 1 HISTORIA_MEDICA (una por cita atendida)
                   └── N HISTORIA_MEDICA 1 ── N ANOTACION_HISTORIA
                                         1 ── N FORMULA_MEDICA
```

| Tabla | Campos principales | Notas |
|---|---|---|
| `cita` | fecha_cita, motivo, estado, mascota_id, medico_id | El cliente se obtiene de la mascota (no se duplica). Estado con CHECK. Índices por médico/fecha y mascota/fecha. |
| `historia_medica` | cita_id (UNIQUE), mascota_id, medico_id, fecha_consulta, motivo_consulta, diagnostico, observaciones, estado | Estado `ABIERTA` / `CERRADA`. |
| `anotacion_historia` | historia_id, medico_id, fecha_anotacion, descripcion, observacion, diagnostico_evolucion | Solo en historias abiertas. |
| `formula_medica` | mascota_id, historia_id, medico_id, fecha_emision, medicamento, dosis, unidad_dosis, frecuencia_horas, duracion_dias, indicaciones, activo | Borrado lógico (`activo`). CHECK en dosis, frecuencia y duración. |

Todas las relaciones tienen llave foránea, por lo que no puede quedar información huérfana; además **no existe
ningún endpoint DELETE** para citas, historias, anotaciones ni fórmulas.

## 3. Endpoints implementados (base `/clinica/v1`)

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/cliente/all` | Listar clientes |
| GET | `/cliente/{id}` | Consultar un cliente |
| GET | `/cliente/{id}/mascotas` | Listar mascotas de un cliente (combos dependientes) |
| GET | `/mascota/{id}` | Consultar una mascota |
| GET | `/medico/all` | Listar veterinarios |
| GET | `/cita/listar` | Listar citas paginadas, con filtros y orden |
| GET | `/cita/{id}` | Consultar una cita |
| POST | `/cita` | Crear cita (201) |
| PUT | `/cita/{id}` | Editar cita (fecha, hora, veterinario, motivo) |
| PATCH | `/cita/{id}/estado` | Cambiar estado (programar, confirmar, atender, cancelar) |
| PATCH | `/cita/{id}/reactivar` | Reactivar una cita cancelada (operación explícita) |
| GET | `/cita/mascota/{id}/atendidas-sin-historia` | Citas atendidas sin historia (combo del formulario de historias) |
| GET | `/historia-medica/listar` | Listar historias clínicas paginadas, con filtros y orden |
| GET | `/historia-medica/{id}` | Consultar historia |
| POST | `/historia-medica` | Crear historia desde una cita atendida (201) |
| PATCH | `/historia-medica/{id}/cerrar` | Cerrar historia |
| GET | `/historia-medica/mascota/{id}` | Historias de una mascota (combo del formulario de fórmulas) |
| POST | `/anotacion-historia` | Crear anotación (201) |
| GET | `/anotacion-historia/historia/{id}` | Anotaciones de una historia (paginado, filtros, orden) |
| GET | `/anotacion-historia/mascota/{id}` | Histórico completo de la mascota |
| GET | `/anotacion-historia/{id}` | Detalle de una anotación |
| GET | `/formula-medica/listar` | Listar fórmulas (vista resumida) paginadas, con filtros y orden |
| GET | `/formula-medica/{id}` | Detalle completo de una fórmula |
| POST | `/formula-medica` | Crear fórmula (201) |
| PUT | `/formula-medica/{id}` | Editar fórmula |
| PATCH | `/formula-medica/{id}/inactivar` | Inactivar fórmula (borrado lógico) |

Parámetros comunes de los listados: `pagina` (desde 0), `tamanio` (máx. 100), `ordenarPor`, `direccion` (`asc`/`desc`),
`texto`, `fechaDesde`, `fechaHasta`, `medicoId`; además `estado` (citas, historias) y `activo` (fórmulas).
Respuesta paginada: `{ contenido, pagina, tamanio, totalRegistros, totalPaginas }`.

Códigos HTTP: **200** consulta/edición, **201** creación, **400** datos inválidos, **404** registro inexistente,
**409** regla de negocio en conflicto (agenda ocupada, cita cancelada/atendida, duplicados), **500** error inesperado
(con mensaje genérico, sin detalles técnicos). Todos los errores tienen la forma `{ status, error, message, timestamp }`.

## 4. Principales reglas de negocio (validadas en el backend)

**Citas**
- Cliente, mascota y veterinario deben existir (404); el cliente debe estar activo y la mascota debe ser suya (400).
- No se registran citas en fechas u horas pasadas.
- Un veterinario no puede tener otra cita activa **a menos de 15 minutos** antes o después; la mascota tampoco (409).
  Las citas canceladas no bloquean la agenda.
- Estados: PENDIENTE, PROGRAMADA, CONFIRMADA, ATENDIDA, CANCELADA. Una cita nueva solo puede ser PENDIENTE,
  PROGRAMADA o CONFIRMADA (no puede nacer atendida ni cancelada).
- Transiciones permitidas: PENDIENTE → PROGRAMADA/CONFIRMADA/ATENDIDA/CANCELADA; PROGRAMADA → CONFIRMADA/ATENDIDA/CANCELADA;
  CONFIRMADA → ATENDIDA/CANCELADA. ATENDIDA y CANCELADA son estados finales.
- No se marca como atendida una cita de un día futuro.

**Historias y anotaciones**
- Se crean solo desde una cita ATENDIDA, de la misma mascota, y una cita tiene como máximo una historia.
- La fecha de consulta no puede ser anterior al día de la cita ni futura.
- Las anotaciones solo se agregan a historias ABIERTAS, con fecha entre la consulta y el momento actual.

**Fórmulas**
- La mascota debe existir y pertenecer a un cliente registrado; el veterinario debe existir.
- La consulta (historia) debe ser de la misma mascota; la fecha de emisión no puede ser anterior a la consulta ni futura.
- Dosis > 0 (máx. 10.000, 2 decimales), unidad de una lista cerrada, frecuencia 1–72 horas, duración 1–365 días.
- No se permiten dos fórmulas activas del mismo medicamento en la misma consulta.
- Nunca se eliminan: se inactivan, y una fórmula inactiva no se puede editar.

## 5. Restricciones de edición y su justificación

- **Cita pendiente/programada/confirmada**: se pueden cambiar fecha, hora, veterinario y motivo, revalidando fecha y agenda.
  No se cambia la mascota ni el cliente porque identifican al paciente; si hay error se cancela y se crea otra cita.
- **Cita cancelada**: no se edita ni cambia de estado, para que no parezca activa. Solo vuelve con la acción explícita
  *Reactivar*, que la deja PENDIENTE en una fecha futura con disponibilidad.
- **Cita atendida**: no se modifica porque es información histórica que soporta la historia clínica.
- **Fórmula**: no se cambia la mascota ni la consulta (definen a quién y en qué atención se formuló); si están mal se
  inactiva y se registra una nueva. Así se conserva la trazabilidad clínica.
- **Historia cerrada**: no admite más anotaciones.

## 6. Decisiones técnicas relevantes

- **Arquitectura por capas** respetando la estructura del curso: `api` (contrato REST) → `apicontroller` (solo delega) →
  `service` / `serviceimpl` (reglas de negocio) → `repository` → `entity`; DTOs en `models`.
- **`ValidadorRelaciones`**: componente reutilizado por todos los servicios para obtener cliente, mascota, veterinario,
  cita e historia y validar su coherencia; evita repetir la misma validación en cada servicio.
- **Excepciones por tipo** (`BadRequestException` 400, `NotFoundException` 404, `ConflictException` 409) que heredan de
  `ApiException`, y un `GlobalExceptionHandler` que traduce también JSON mal formado, parámetros inválidos y errores
  de integridad, sin exponer trazas.
- **Paginación, filtros y orden en el servidor** con Spring Data `Specification` + `Pageable`. El orden solo se permite
  por columnas de una lista blanca (`PaginacionUtil`), así el cliente no puede ordenar por campos internos.
- **Estados como `enum`** (`EstadoCita`, `EstadoHistoria`) con las transiciones definidas en el propio enum.
- **Listado vs. detalle de fórmulas**: el listado retorna un DTO resumido (`FormulaMedicaResumenRs`, con la posología en una
  línea); el detalle retorna la fórmula completa con mascota, cliente, consulta y veterinario.
- **Frontend Angular**: servicios por módulo que consumen la API, formularios reactivos con validaciones (también de fechas
  y decimales), combos dependientes Cliente → Mascota → Cita/Historia, clase base `TablaPaginada` reutilizada por todos los
  DataTables, y componentes compartidos (`app-paginacion`, columna ordenable, `app-estado-badge`, `app-cargando`).
  Los errores se muestran con un título según el código HTTP (`http-error.util.ts`) e indicando si el backend no responde.
- **Pruebas**: 39 pruebas unitarias de reglas de negocio (Mockito) y una prueba de integración del flujo completo
  contra H2 en memoria.
