# Clinica - Taller Prog. Web (CRUD Usuario)

API REST en Spring Boot para gestionar la entidad `Usuario` de la clínica.

## Requisitos cubiertos

- **Listar usuarios** ordenados alfabéticamente (A-Z) por nombre, sin exponer el password.
- **Guardar usuario** (crear), cifrando el password en MD5.
- **Actualizar usuario**, re-cifrando el password en MD5 solo si se envía uno nuevo.

## Endpoints

| Método | Ruta                              | Descripción                 |
|--------|-----------------------------------|------------------------------|
| GET    | `/clinica/v1/user/listar-usuarios`   | Lista usuarios (A-Z, sin password) |
| POST   | `/clinica/v1/user/guardar-usuario`   | Crea un usuario (password cifrado en MD5) |
| PUT    | `/clinica/v1/user/actualizar-usuario`| Actualiza un usuario (por `id` en el body) |

### Body de ejemplo (guardar / actualizar)

```json
{
  "id": 1,
  "nombre": "Ana Torres",
  "email": "ana@example.com",
  "password": "secreto123"
}
```

## Configuración

Editar `src/main/resources/application.properties` con los datos de tu MySQL local (base de datos `clinica`).

## Ejecutar

```bash
./mvnw spring-boot:run
```

## Autores

- Jose David Aguilar
- Juan David Gomez
- Wilson Marin Gallego
- Jose David Lopez
- Nel Felipe Poveda Peña
