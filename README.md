# backend-83681
Backend para una clinica veterinaria

## Configuración local

La aplicación usa el perfil `dev` por defecto. Antes de iniciarla, define las
variables de entorno `DB_USERNAME` y `DB_PASSWORD` con las credenciales de una
cuenta MySQL con permisos limitados sobre la base de datos `clinica`. No guardes
esas credenciales en los archivos de propiedades.

En PowerShell, configura las variables para la sesión actual y ejecuta el
backend así:

```powershell
$env:DB_USERNAME = "usuario_local"
$env:DB_PASSWORD = "contraseña_local"
.\mvnw.cmd spring-boot:run
```

Para ejecutar las pruebas:

```powershell
.\mvnw.cmd test
```

En producción, establece `SPRING_PROFILES_ACTIVE=prod` y configura las mismas
variables de base de datos en el entorno de ejecución.

## Catálogo de medicamentos

La tabla `medicamento` se incluye en `sql/clinica_parcial2.sql` para bases de
datos nuevas. Para una base `clinica` ya existente, ejecuta
`sql/migracion_medicamento.sql` una sola vez antes de iniciar el backend. No
vuelvas a ejecutar `clinica_parcial2.sql` sobre una base con datos que quieras
conservar: ese script elimina y recrea la base completa.
