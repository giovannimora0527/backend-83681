param(
    [string]$Name = "Uploader",
    [string]$Email = "uploader@example.com",
    [string]$RemoteUrl = "https://github.com/giovannimora0527/backend-83681",
    [string]$Branch = "904876_JoseAguilar_parcial"
)

# Comprueba si git está instalado
if (-not (Get-Command git -ErrorAction SilentlyContinue)) {
    Write-Error "Git no está instalado o no está en PATH. Instala Git desde https://git-scm.com/download/win y vuelve a ejecutar este script."
    exit 1
}

# Ir al directorio del script (proyecto)
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $scriptDir
Write-Output "Working directory: $PWD"

# Inicializar repo si no existe
if (-not (Test-Path .git)) {
    git init
    Write-Output "Repositorio git inicializado."
} else {
    Write-Output ".git ya existe; usando repositorio existente."
}

# Añadir y commitear
git add .
try {
    git -c user.name="$Name" -c user.email="$Email" commit -m "Initial import of Proyecto_Completo"
} catch {
    Write-Output "No hay cambios para commitear o el commit falló: $_"
}

# Crear y cambiar a la rama solicitada
$existingBranch = git branch --list $Branch
if ([string]::IsNullOrEmpty($existingBranch)) {
    git checkout -b $Branch
    Write-Output "Rama '$Branch' creada y seleccionada."
} else {
    git checkout $Branch
    Write-Output "Rama '$Branch' ya existe; seleccionada."
}

# Configurar remoto
try {
    git remote add origin $RemoteUrl 2>$null
    Write-Output "Remote 'origin' añadido: $RemoteUrl"
} catch {
    Write-Output "Remote 'origin' probablemente ya existe. Actualizando URL..."
    git remote set-url origin $RemoteUrl
}

# Forzar push de la rama al remoto (pedirá credenciales si es necesario)
Write-Output "Empujando la rama '$Branch' al remoto '$RemoteUrl'..."
git push -u origin $Branch

Write-Output "Hecho. Si hay errores de autenticación, inicia sesión con 'gh auth login' o usa un PAT."