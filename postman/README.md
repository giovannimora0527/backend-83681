# Colección de Postman - Clínica Veterinaria (Segundo Parcial)

1. Ejecute `sql/clinica_parcial2.sql` en MySQL y levante el backend (puerto 8080, contexto `/clinica/v1`).
2. En Postman: **Import** → `Clinica_Veterinaria.postman_collection.json`.
3. La variable de colección `base_url` vale `http://localhost:8080/clinica/v1`.
4. Algunas peticiones tienen valores entre `<>` (fechas): reemplácelos por fechas válidas según el día en que pruebe,
   porque los datos de prueba se generan relativos a la fecha actual.

Las carpetas incluyen casos de error a propósito (409 veterinario ocupado, 400 mascota de otro cliente) para
demostrar el manejo de reglas de negocio.
