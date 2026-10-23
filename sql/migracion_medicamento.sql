USE clinica;

CREATE TABLE IF NOT EXISTS medicamento (
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre             VARCHAR(100) NOT NULL,
    presentacion       VARCHAR(80)  NOT NULL,
    descripcion        TEXT,
    fecha_creacion     DATETIME,
    fecha_modificacion DATETIME,
    CONSTRAINT uk_medicamento_nombre_presentacion UNIQUE (nombre, presentacion)
);
