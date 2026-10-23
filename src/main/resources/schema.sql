-- =========================================================
-- Script COMPLETO de la base de datos - Clinica Veterinaria
-- Segundo parcial de Programacion Web - NRC 83861
-- Para una base NUEVA. Si ya tiene la base del primer parcial,
-- use migracion_parcial2.sql en lugar de este archivo.
-- =========================================================

DROP DATABASE IF EXISTS clinica;
CREATE DATABASE clinica CHARACTER SET utf8mb4;
USE clinica;

-- ---------------------------------------------------------
-- Catalogos y personas
-- ---------------------------------------------------------
CREATE TABLE raza (
    raza_id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100),
    especie VARCHAR(100),
    fecha_creacion DATETIME,
    fecha_modificacion DATETIME
);

CREATE TABLE cliente (
    cliente_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tipo_documento VARCHAR(20),
    numero_documento VARCHAR(30),
    nombres VARCHAR(100),
    apellidos VARCHAR(100),
    fecha_nacimiento DATE,
    genero VARCHAR(20),
    telefono VARCHAR(20),
    direccion VARCHAR(200),
    activo BOOLEAN
);

CREATE TABLE mascota (
    mascota_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre_mascota VARCHAR(100),
    edad INT,
    fecha_registro DATETIME,
    fecha_modificacion DATETIME,
    raza_id INT,
    cliente_id BIGINT,
    FOREIGN KEY (raza_id) REFERENCES raza(raza_id),
    FOREIGN KEY (cliente_id) REFERENCES cliente(cliente_id)
);

CREATE TABLE especializacion (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    descripcion TEXT,
    codigo_especializacion VARCHAR(10) NOT NULL
);

CREATE TABLE medico (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tipo_documento VARCHAR(10) NOT NULL,
    numero_documento VARCHAR(20) NOT NULL,
    nombres VARCHAR(100) NOT NULL,
    apellidos VARCHAR(100) NOT NULL,
    telefono VARCHAR(20),
    registro_profesional VARCHAR(50) NOT NULL,
    especializacion_id BIGINT NOT NULL,
    FOREIGN KEY (especializacion_id) REFERENCES especializacion(id)
);

CREATE TABLE usuario (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    rol VARCHAR(20) NOT NULL,
    fecha_creacion DATETIME NOT NULL,
    activo BOOLEAN DEFAULT TRUE,
    email VARCHAR(100)
);

CREATE TABLE medicamento (
    medicamento_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    presentacion VARCHAR(50),
    descripcion VARCHAR(255)
);

-- ---------------------------------------------------------
-- Citas: Mascota 1:N Cita, Cita N:1 Veterinario
-- ---------------------------------------------------------
CREATE TABLE citas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    mascota_id BIGINT NOT NULL,
    medico_id BIGINT NOT NULL,
    fecha DATETIME NOT NULL,
    motivo VARCHAR(200) NOT NULL,
    observaciones VARCHAR(255),
    estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    FOREIGN KEY (mascota_id) REFERENCES mascota(mascota_id),
    FOREIGN KEY (medico_id) REFERENCES medico(id),
    INDEX idx_citas_medico_fecha (medico_id, fecha),
    INDEX idx_citas_mascota_fecha (mascota_id, fecha)
);

-- ---------------------------------------------------------
-- Historias clinicas: Mascota 1:N Historia; una cita genera maximo una historia
-- ---------------------------------------------------------
CREATE TABLE historias_medicas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    mascota_id BIGINT NOT NULL,
    cita_id BIGINT NULL,
    medico_id BIGINT NULL,
    fecha_consulta DATETIME NULL,
    fecha_creacion DATETIME NOT NULL,
    motivo_consulta VARCHAR(200),
    diagnostico VARCHAR(500),
    observaciones_generales VARCHAR(500),
    estado VARCHAR(20) NOT NULL DEFAULT 'ABIERTA',
    FOREIGN KEY (mascota_id) REFERENCES mascota(mascota_id),
    FOREIGN KEY (cita_id) REFERENCES citas(id),
    FOREIGN KEY (medico_id) REFERENCES medico(id),
    CONSTRAINT uq_historia_cita UNIQUE (cita_id)
);

CREATE TABLE anotaciones_historia (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    historia_medica_id BIGINT NOT NULL,
    medico_id BIGINT NULL,
    descripcion VARCHAR(500) NOT NULL,
    observacion VARCHAR(500),
    diagnostico_evolucion VARCHAR(500),
    fecha DATETIME NOT NULL,
    FOREIGN KEY (historia_medica_id) REFERENCES historias_medicas(id),
    FOREIGN KEY (medico_id) REFERENCES medico(id)
);

-- ---------------------------------------------------------
-- Formulas medicas: Historia 1:N Formula (no se eliminan, se inactivan)
-- ---------------------------------------------------------
CREATE TABLE formula_medica (
    formula_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    mascota_id BIGINT NOT NULL,
    historia_medica_id BIGINT NULL,
    medico_id BIGINT NOT NULL,
    medicamento_id BIGINT NULL,
    fecha_creacion DATETIME NOT NULL,
    fecha_emision DATETIME NULL,
    dosis DECIMAL(10,2) NULL,
    unidad_dosis VARCHAR(20) NULL,
    frecuencia_horas INT NULL,
    duracion_dias INT NULL,
    indicaciones VARCHAR(500) NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVA',
    FOREIGN KEY (mascota_id) REFERENCES mascota(mascota_id),
    FOREIGN KEY (historia_medica_id) REFERENCES historias_medicas(id),
    FOREIGN KEY (medico_id) REFERENCES medico(id),
    FOREIGN KEY (medicamento_id) REFERENCES medicamento(medicamento_id)
);

-- Tabla del primer parcial (detalle de medicamentos por formula). Ya no la usa el sistema,
-- se conserva para no perder informacion historica.
CREATE TABLE formula_medica_detalle (
    detalle_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    formula_id BIGINT NOT NULL,
    medicamento_id BIGINT NOT NULL,
    dosis VARCHAR(50),
    frecuencia VARCHAR(50),
    duracion_dias INT,
    FOREIGN KEY (formula_id) REFERENCES formula_medica(formula_id),
    FOREIGN KEY (medicamento_id) REFERENCES medicamento(medicamento_id)
);
