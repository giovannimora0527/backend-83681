# Backend - Clínica Veterinaria

API desarrollada con Spring Boot y MySQL para la gestión de una clínica veterinaria.

## Requisitos

- Java 21
- MySQL
- Una base de datos llamada clinica

## Configuración local

Configura estas variables en tu terminal antes de iniciar la aplicación:

- DB_URL: URL JDBC de MySQL (por defecto apunta a localhost:3306/clinica).
- DB_USER: usuario de MySQL.
- DB_PASSWORD: contraseña local de MySQL.
- JWT_SECRET: clave aleatoria codificada en Base64 de al menos 64 bytes.
- MAIL_USERNAME, MAIL_PASSWORD y MAIL_TO: datos SMTP, si se habilitan notificaciones por correo.

No guardes contraseñas ni claves JWT en este repositorio. También puedes definir propiedades privadas en application-secrets.properties, que está excluido de Git.

En PowerShell, por ejemplo:

    $env:DB_URL = 'jdbc:mysql://localhost:3306/clinica?serverTimezone=America/Bogota&useSSL=false&allowPublicKeyRetrieval=true'
    $env:DB_USER = 'root'
    $env:DB_PASSWORD = 'TU_CLAVE_LOCAL'
    $env:JWT_SECRET = 'TU_CLAVE_BASE64_DE_64_BYTES'
    .\mvnw.cmd spring-boot:run