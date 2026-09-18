# Colección de Postman - Clínica Veterinaria API

Este directorio contiene la colección [`Clinica_Veterinaria.postman_collection.json`](./Clinica_Veterinaria.postman_collection.json) con todos los servicios REST del proyecto, lista para importar en Postman.

## 1. Requisitos previos

- Tener **Postman** instalado (Desktop o web).
- Tener el proyecto corriendo localmente:
  - Perfil activo: `dev` (`spring.profiles.active=dev`)
  - Puerto: `8080`
  - Context path: `/clinica/v1`
  - Base de datos MySQL `clinica` levantada y accesible (ver `spring.datasource.url` en `application-dev.properties`)
- Que existan datos previos de `mascota` y `medico` en la BD (los ejemplos usan `mascotaId: 1` y `medicoId: 1`). Puedes usar los scripts de la carpeta [`../sql/`](../sql/) para poblarlos.

## 2. Cómo importar el JSON en Postman

1. Abre Postman.
2. Haz clic en el botón **Import** (esquina superior izquierda).
3. En la ventana que se abre, elige la pestaña **File** y arrastra (o selecciona con "Choose Files") el archivo:
   ```
   Proyecto_Completo/postman/Clinica_Veterinaria.postman_collection.json
   ```
4. Haz clic en **Import**.
5. En el panel izquierdo, bajo **Collections**, aparecerá **"Clinica Veterinaria API"** con 4 carpetas:
   - `Formula Medica`
   - `Cita`
   - `Historia Medica`
   - `Anotacion Historia`

### Verificar/editar la variable `base_url`

La colección ya trae configurada la variable `base_url = http://localhost:8080/clinica/v1`. Para revisarla o cambiarla (por ejemplo si usas otro puerto o perfil):

1. Clic derecho sobre la colección **"Clinica Veterinaria API"** → **Edit**.
2. Ve a la pestaña **Variables**.
3. Ajusta el valor de `base_url` en la columna **Current value** si es necesario.
4. Guarda los cambios (**Save**).

Todos los requests usan `{{base_url}}` en la URL, así que basta con cambiar esa variable una sola vez.

## 3. Levantar la aplicación antes de probar

Desde la raíz de `Proyecto_Completo`:

```powershell
.\mvnw.cmd spring-boot:run
```

o ejecuta la clase `ClinicaApplication` desde tu IDE. Espera a ver el log de Spring Boot indicando que el servidor arrancó en el puerto `8080`.

## 4. Endpoints incluidos

> ⚠️ Nota importante: los endpoints `GET` de este proyecto exigen el header `Content-Type: application/json` aunque no envíen cuerpo (así están declarados con `consumes = "application/json"` en el contrato). Ya vienen configurados en la colección, pero si creas un request nuevo a mano, no lo olvides — si falta, Spring responde `415 Unsupported Media Type`.

### 4.1 Fórmula Médica

| Request | Método | Endpoint |
|---|---|---|
| Listar fórmulas médicas (orden desc por fecha creación) | `GET` | `{{base_url}}/formula-medica/listar-ordenado` |

Sin query params ni body.

---

### 4.2 Cita

| Request | Método | Endpoint |
|---|---|---|
| Filtrar citas por rango de fechas (orden desc) | `GET` | `{{base_url}}/cita/filtrar?fechaInicial=...&fechaFinal=...` |
| Crear cita | `POST` | `{{base_url}}/cita/guardar` |
| Actualizar cita | `POST` | `{{base_url}}/cita/actualizar` |

**Query params (filtrar):**

| Param | Formato | Ejemplo |
|---|---|---|
| `fechaInicial` | `yyyy-MM-dd'T'HH:mm:ss` | `2026-01-01T00:00:00` |
| `fechaFinal` | `yyyy-MM-dd'T'HH:mm:ss` | `2026-12-31T23:59:59` |

**Body - Crear cita** (`POST /cita/guardar`):

```json
{
  "fechaCita": "2026-09-20T10:30:00",
  "motivo": "Control de vacunación",
  "estado": "PENDIENTE",
  "mascotaId": 1,
  "medicoId": 1
}
```

**Body - Actualizar cita** (`POST /cita/actualizar`, requiere `citaId` existente):

```json
{
  "citaId": 1,
  "fechaCita": "2026-09-21T15:00:00",
  "motivo": "Control de vacunación (reprogramada)",
  "estado": "CONFIRMADA",
  "mascotaId": 1,
  "medicoId": 1
}
```

| Campo | Tipo | Obligatorio |
|---|---|---|
| `citaId` | Long | Solo en actualizar |
| `fechaCita` | LocalDateTime | Sí |
| `motivo` | String | Sí |
| `estado` | String | Sí |
| `mascotaId` | Long | Sí (debe existir) |
| `medicoId` | Long | Sí (debe existir) |

---

### 4.3 Historia Médica

| Request | Método | Endpoint |
|---|---|---|
| Crear historia médica | `POST` | `{{base_url}}/historia-medica/guardar` |
| Listar historias por rango de fechas (orden desc) | `GET` | `{{base_url}}/historia-medica/listar?fechaInicial=...&fechaFinal=...` |
| Actualizar historia médica | `POST` | `{{base_url}}/historia-medica/actualizar` |

**Body - Crear historia médica:**

```json
{
  "diagnostico": "Otitis leve",
  "tratamiento": "Limpieza ótica y gotas antibióticas",
  "mascotaId": 1,
  "medicoId": 1
}
```

**Body - Actualizar historia médica** (requiere `historiaId` existente):

```json
{
  "historiaId": 1,
  "diagnostico": "Otitis leve resuelta",
  "tratamiento": "Finalizado tratamiento antibiótico",
  "mascotaId": 1,
  "medicoId": 1
}
```

**Query params (listar):** iguales a los de `cita/filtrar` (`fechaInicial`, `fechaFinal`).

---

### 4.4 Anotación Historia

| Request | Método | Endpoint |
|---|---|---|
| Crear anotación de historia | `POST` | `{{base_url}}/anotacion-historia/guardar` |
| Listar anotaciones de una historia (orden desc) | `GET` | `{{base_url}}/anotacion-historia/listar?historiaId=...` |
| Actualizar anotación de historia | `POST` | `{{base_url}}/anotacion-historia/actualizar` |

**Body - Crear anotación:**

```json
{
  "observacion": "Se observa mejoría notable tras 5 días de tratamiento",
  "historiaId": 1,
  "medicoId": 1
}
```

**Body - Actualizar anotación** (requiere `anotacionId` existente):

```json
{
  "anotacionId": 1,
  "observacion": "Observación corregida: mejoría total, alta médica",
  "historiaId": 1,
  "medicoId": 1
}
```

**Query param (listar):**

| Param | Tipo | Ejemplo |
|---|---|---|
| `historiaId` | Long | `1` |

## 5. Flujo sugerido de pruebas

1. `POST /historia-medica/guardar` → anota el `historiaId` que devuelva la respuesta/BD.
2. `POST /anotacion-historia/guardar` usando ese `historiaId`.
3. `GET /anotacion-historia/listar?historiaId=<id>` → verifica que aparezca ordenada de más reciente a más antigua.
4. `GET /historia-medica/listar` con un rango de fechas que incluya la fecha de creación.
5. `POST /cita/guardar` → luego `GET /cita/filtrar` con un rango que la incluya.
6. `POST /cita/actualizar` sobre la cita creada.
7. `GET /formula-medica/listar-ordenado` (requiere datos previos en la tabla `formula_medica`, ver [`../sql/`](../sql/)).

## 6. Errores comunes

| Código | Causa probable | Solución |
|---|---|---|
| `415 Unsupported Media Type` | Falta el header `Content-Type: application/json` en un GET | Agregar el header manualmente |
| `400 Bad Request` con mensaje de negocio | Falta un campo obligatorio, o `mascotaId`/`medicoId`/`historiaId` no existen en BD | Revisar el body contra las tablas o el mensaje de `BadRequestException` |
| `Connection refused` | La app no está corriendo o el puerto/perfil no coincide | Verificar `application-dev.properties` y que el server esté levantado |
| Fechas rechazadas (`400`) | Formato de fecha inválido en query params | Usar exactamente `yyyy-MM-dd'T'HH:mm:ss` (ISO local date-time) |
