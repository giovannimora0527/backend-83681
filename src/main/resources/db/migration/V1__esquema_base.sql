CREATE TABLE IF NOT EXISTS perfil (
    perfil_id INT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(50) NOT NULL,
    activo BIT NOT NULL,
    fecha_creacion DATETIME(6) NOT NULL,
    fecha_modificacion DATETIME(6) NULL,
    PRIMARY KEY (perfil_id),
    UNIQUE KEY uk_perfil_nombre (nombre)
) ENGINE=InnoDB;

INSERT INTO perfil (nombre, activo, fecha_creacion)
SELECT 'ADMINISTRADOR', b'1', CURRENT_TIMESTAMP(6)
WHERE NOT EXISTS (SELECT 1 FROM perfil WHERE nombre = 'ADMINISTRADOR');

INSERT INTO perfil (nombre, activo, fecha_creacion)
SELECT 'VETERINARIO', b'1', CURRENT_TIMESTAMP(6)
WHERE NOT EXISTS (SELECT 1 FROM perfil WHERE nombre = 'VETERINARIO');

INSERT INTO perfil (nombre, activo, fecha_creacion)
SELECT 'DESPACHO_MEDICO', b'1', CURRENT_TIMESTAMP(6)
WHERE NOT EXISTS (SELECT 1 FROM perfil WHERE nombre = 'DESPACHO_MEDICO');

INSERT INTO perfil (nombre, activo, fecha_creacion)
SELECT 'RECEPCIONISTA', b'1', CURRENT_TIMESTAMP(6)
WHERE NOT EXISTS (SELECT 1 FROM perfil WHERE nombre = 'RECEPCIONISTA');

CREATE TABLE IF NOT EXISTS especializacion (
    id BIGINT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(255) NULL,
    descripcion VARCHAR(255) NULL,
    codigo_especializacion VARCHAR(255) NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS raza (
    raza_id INT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(100) NOT NULL,
    especie VARCHAR(100) NOT NULL,
    fecha_creacion DATETIME(6) NOT NULL,
    fecha_modificacion DATETIME(6) NULL,
    PRIMARY KEY (raza_id),
    KEY ix_raza_especie_nombre (especie, nombre)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS cliente (
    cliente_id BIGINT NOT NULL AUTO_INCREMENT,
    usuario_id INT NULL,
    tipo_documento VARCHAR(10) NOT NULL,
    numero_documento VARCHAR(20) NOT NULL,
    nombres VARCHAR(100) NOT NULL,
    apellidos VARCHAR(100) NOT NULL,
    fecha_nacimiento DATE NOT NULL,
    genero VARCHAR(1) NULL,
    telefono VARCHAR(20) NULL,
    direccion TEXT NULL,
    activo BIT NOT NULL,
    PRIMARY KEY (cliente_id),
    UNIQUE KEY uk_cliente_usuario (usuario_id),
    UNIQUE KEY uk_cliente_documento (numero_documento)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS mascota (
    mascota_id INT NOT NULL AUTO_INCREMENT,
    nombre_mascota VARCHAR(255) NULL,
    edad INT NULL,
    fecha_registro DATETIME(6) NULL,
    fecha_modificacion DATETIME(6) NULL,
    cliente_id BIGINT NULL,
    raza_id INT NULL,
    activo BIT NULL,
    PRIMARY KEY (mascota_id),
    KEY ix_mascota_cliente (cliente_id),
    KEY ix_mascota_raza (raza_id),
    CONSTRAINT fk_mascota_cliente FOREIGN KEY (cliente_id) REFERENCES cliente (cliente_id),
    CONSTRAINT fk_mascota_raza FOREIGN KEY (raza_id) REFERENCES raza (raza_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS medico (
    id BIGINT NOT NULL AUTO_INCREMENT,
    tipo_documento VARCHAR(10) NOT NULL,
    numero_documento VARCHAR(20) NOT NULL,
    nombres VARCHAR(100) NOT NULL,
    apellidos VARCHAR(100) NOT NULL,
    telefono VARCHAR(20) NULL,
    registro_profesional VARCHAR(50) NOT NULL,
    especializacion_id BIGINT NOT NULL,
    estado BIT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_medico_documento (numero_documento),
    UNIQUE KEY uk_medico_registro (registro_profesional),
    KEY ix_medico_especializacion (especializacion_id),
    CONSTRAINT fk_medico_especializacion FOREIGN KEY (especializacion_id) REFERENCES especializacion (id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS usuario (
    id BIGINT NOT NULL AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL,
    password_hash TEXT NULL,
    rol INT NOT NULL,
    fecha_creacion DATETIME(6) NULL,
    activo BIT NULL,
    email VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_usuario_username (username),
    UNIQUE KEY uk_usuario_email (email),
    KEY ix_usuario_rol (rol),
    CONSTRAINT fk_usuario_perfil FOREIGN KEY (rol) REFERENCES perfil (perfil_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS medicamento (
    id BIGINT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(100) NOT NULL,
    descripcion TEXT NULL,
    presentacion VARCHAR(100) NULL,
    fecha_compra DATE NOT NULL,
    fecha_vence DATE NOT NULL,
    fecha_creacion_registro DATETIME(6) NOT NULL,
    fecha_modificacion_registro DATETIME(6) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_medicamento_nombre (nombre)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS cita (
    id BIGINT NOT NULL AUTO_INCREMENT,
    cliente_id BIGINT NOT NULL,
    mascota_id INT NOT NULL,
    medico_id BIGINT NOT NULL,
    fecha_hora DATETIME(6) NOT NULL,
    estado VARCHAR(20) NOT NULL,
    motivo TEXT NULL,
    PRIMARY KEY (id),
    KEY ix_cita_cliente (cliente_id),
    KEY ix_cita_mascota_fecha (mascota_id, fecha_hora),
    KEY ix_cita_medico_fecha (medico_id, fecha_hora),
    CONSTRAINT fk_cita_cliente FOREIGN KEY (cliente_id) REFERENCES cliente (cliente_id),
    CONSTRAINT fk_cita_mascota FOREIGN KEY (mascota_id) REFERENCES mascota (mascota_id),
    CONSTRAINT fk_cita_medico FOREIGN KEY (medico_id) REFERENCES medico (id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS historia_medica (
    id BIGINT NOT NULL AUTO_INCREMENT,
    paciente_id INT NOT NULL,
    fecha_creacion DATETIME(6) NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    KEY ix_historia_mascota (paciente_id),
    CONSTRAINT fk_historia_mascota FOREIGN KEY (paciente_id) REFERENCES mascota (mascota_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS anotacion_historia (
    id BIGINT NOT NULL AUTO_INCREMENT,
    historia_id BIGINT NOT NULL,
    medico_id BIGINT NOT NULL,
    cita_id BIGINT NOT NULL,
    fecha DATETIME(6) NULL DEFAULT CURRENT_TIMESTAMP(6),
    descripcion TEXT NOT NULL,
    diagnostico TEXT NULL,
    observaciones TEXT NULL,
    PRIMARY KEY (id),
    KEY ix_anotacion_historia (historia_id),
    KEY ix_anotacion_medico (medico_id),
    KEY ix_anotacion_cita (cita_id),
    CONSTRAINT fk_anotacion_historia FOREIGN KEY (historia_id) REFERENCES historia_medica (id),
    CONSTRAINT fk_anotacion_medico FOREIGN KEY (medico_id) REFERENCES medico (id),
    CONSTRAINT fk_anotacion_cita FOREIGN KEY (cita_id) REFERENCES cita (id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS formula_medica (
    id BIGINT NOT NULL AUTO_INCREMENT,
    cita_id BIGINT NOT NULL,
    medicamento_id BIGINT NOT NULL,
    dosis TEXT NOT NULL,
    indicaciones TEXT NULL,
    fecha_creacion_registro DATETIME(6) NOT NULL,
    fecha_actualizacion_registro DATETIME(6) NULL,
    frecuencia VARCHAR(255) NULL,
    duracion VARCHAR(255) NULL,
    PRIMARY KEY (id),
    KEY ix_formula_cita (cita_id),
    KEY ix_formula_medicamento (medicamento_id),
    CONSTRAINT fk_formula_cita FOREIGN KEY (cita_id) REFERENCES cita (id),
    CONSTRAINT fk_formula_medicamento FOREIGN KEY (medicamento_id) REFERENCES medicamento (id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS menu (
    menu_id INT NOT NULL AUTO_INCREMENT,
    code VARCHAR(100) NOT NULL,
    title VARCHAR(255) NOT NULL,
    url LONGTEXT NOT NULL,
    icon VARCHAR(255) NOT NULL,
    activo BIT NOT NULL,
    fecha_creacion DATETIME(6) NOT NULL,
    fecha_modificacion DATETIME(6) NULL,
    PRIMARY KEY (menu_id),
    UNIQUE KEY uk_menu_code (code)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS perfil_menu (
    perfil_menu_id INT NOT NULL AUTO_INCREMENT,
    perfil_id INT NOT NULL,
    menu_id INT NOT NULL,
    PRIMARY KEY (perfil_menu_id),
    UNIQUE KEY uk_perfil_menu (perfil_id, menu_id),
    CONSTRAINT fk_perfil_menu_perfil FOREIGN KEY (perfil_id) REFERENCES perfil (perfil_id),
    CONSTRAINT fk_perfil_menu_menu FOREIGN KEY (menu_id) REFERENCES menu (menu_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS otp_token (
    id BIGINT NOT NULL AUTO_INCREMENT,
    usuario_id BIGINT NOT NULL,
    codigo VARCHAR(6) NOT NULL,
    fecha_creacion DATETIME(6) NOT NULL,
    fecha_expiracion DATETIME(6) NOT NULL,
    intentos INT NOT NULL,
    usado BIT NOT NULL,
    PRIMARY KEY (id),
    KEY ix_otp_usuario_fecha (usuario_id, fecha_expiracion)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `session` (
    session_id INT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    token VARCHAR(500) NOT NULL,
    fecha_ini_sesion DATETIME(6) NULL,
    fecha_expiracion DATETIME(6) NULL,
    PRIMARY KEY (session_id),
    UNIQUE KEY uk_session_token (token),
    KEY ix_session_user (user_id)
) ENGINE=InnoDB;
