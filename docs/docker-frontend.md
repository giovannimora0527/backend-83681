# Ejecutar backend y frontend con Docker Compose

El `compose.yaml` ubicado en la raíz del backend levanta MySQL, Spring Boot y Angular servido por Nginx. Desde la carpeta `backend-83681`:

```powershell
docker compose up -d --build
docker compose ps
```

Abre el frontend en [http://localhost:4200](http://localhost:4200). Angular usa la ruta relativa `/clinica/v1`; Nginx reenvía las peticiones al servicio `app` de Compose. También queda disponible directamente la API en `http://localhost:8080/clinica/v1` para diagnóstico.

La aplicación frontend se compila en una etapa Node y se publica desde una imagen Nginx sin privilegios de root. No se copia `node_modules` ni secretos a la imagen. El `dist`, `node_modules`, caché Angular y `.git` están excluidos del contexto por `.dockerignore`.

Para reconstruir solo el frontend tras cambios:

```powershell
docker compose build frontend
docker compose up -d frontend
```

Para ver registros:

```powershell
docker compose logs -f frontend
docker compose logs -f app
```

Si el puerto 4200 está ocupado, configura `FRONTEND_PORT` en `.env` y vuelve a levantar el servicio. Para el desarrollo local con `ng serve`, el ambiente de desarrollo conserva `http://localhost:8080/clinica/v1`.
