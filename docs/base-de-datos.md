# Base de datos del backend

Las migraciones del esquema están en `src/main/resources/db/migration` y Flyway las ejecuta al iniciar la aplicación. La migración `V1__esquema_base.sql` crea las tablas de las entidades existentes cuando la base está vacía y prepara los perfiles iniciales `ADMINISTRADOR`, `VETERINARIO`, `DESPACHO_MEDICO` y `RECEPCIONISTA`.

El backend valida JWT firmados en las rutas protegidas y comprueba que el token tenga una sesión vigente en la tabla `session`. Para crear el primer administrador de una base nueva, configura `BOOTSTRAP_ADMIN_USERNAME`, `BOOTSTRAP_ADMIN_EMAIL` y `BOOTSTRAP_ADMIN_PASSWORD` en `.env`; el inicializador crea el usuario una sola vez con contraseña BCrypt. El acceso de `DESPACHO_MEDICO` queda separado de usuarios, citas, historia clínica y edición de fórmulas.

Para aplicar cambios del esquema, agrega una nueva migración con el siguiente número disponible; no edites una migración que ya se haya ejecutado en una base persistente. El volumen de MySQL de Compose conserva los datos entre reinicios, por lo que no se debe usar `docker compose down -v` para actualizar el esquema.

En una base ya usada, revisa su estructura antes de aplicar una migración inicial. `baseline-on-migrate` permite a Flyway registrar la versión inicial de una base no vacía; no convierte automáticamente tablas antiguas en el esquema nuevo.
