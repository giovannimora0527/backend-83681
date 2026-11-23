# Importación de inventario

La plantilla está en `docs/plantilla-importacion-inventario.csv`. Se aceptan `.csv` y `.xlsx`; ambos deben incluir estas columnas: `codigoProducto`, `codigoLote`, `fechaVencimiento`, `cantidad` y `costoUnitario`. Para CSV use UTF-8 y fechas ISO (`AAAA-MM-DD`). El código de producto debe existir en el catálogo.

El archivo admite hasta 5.000 filas y 10 MB. La previsualización no escribe datos; muestra validaciones por fila. La ejecución registra los lotes válidos individualmente, conserva los errores por fila y deja auditoría del archivo, su SHA-256, usuario, conteos y fecha.

## Rutas

- `POST /clinica/v1/inventario/importaciones/previsualizar`
- `POST /clinica/v1/inventario/importaciones/ejecutar`

Adjunte el archivo como parte multipart `archivo` y envíe el JWT en `Authorization: Bearer <token>`. En PowerShell:

```powershell
$headers = @{ Authorization = "Bearer $token" }
$form = @{ archivo = Get-Item ".\inventario.csv" }
Invoke-RestMethod -Uri "http://localhost:8080/clinica/v1/inventario/importaciones/previsualizar" -Method Post -Headers $headers -Form $form
Invoke-RestMethod -Uri "http://localhost:8080/clinica/v1/inventario/importaciones/ejecutar" -Method Post -Headers $headers -Form $form
```

La cuenta debe tener rol `ADMINISTRADOR` o `VETERINARIO`, igual que para el ingreso manual de lotes.
