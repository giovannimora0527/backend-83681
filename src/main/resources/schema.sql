-- =========================================================
-- Script de creacion de base de datos - Clinica Veterinaria
-- Primer Parcial de Programacion Web - NRC 83861
-- =========================================================

CREATE DATABASE IF NOT EXISTS clinica CHARACTER SET utf8mb4;
USE clinica;

-- ---------------------------------------------------------
-- Tablas base (arquitectura entregada por el profesor)
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

-- ---------------------------------------------------------
-- Punto 2, 3 y 4 - Citas
-- ---------------------------------------------------------

CREATE TABLE citas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    mascota_id BIGINT NOT NULL,
    medico_id BIGINT NOT NULL,
    fecha DATETIME NOT NULL,
    motivo VARCHAR(200) NOT NULL,
    observaciones VARCHAR(255),
    FOREIGN KEY (mascota_id) REFERENCES mascota(mascota_id),
    FOREIGN KEY (medico_id) REFERENCES medico(id)
);

-- ---------------------------------------------------------
-- Punto 1 - Formulas medicas
-- ---------------------------------------------------------

CREATE TABLE medicamento (
    medicamento_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    presentacion VARCHAR(50),
    descripcion VARCHAR(255)
);

CREATE TABLE formula_medica (
    formula_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    mascota_id BIGINT NOT NULL,
    medico_id BIGINT NOT NULL,
    fecha_creacion DATETIME NOT NULL,
    observaciones VARCHAR(255),
    FOREIGN KEY (mascota_id) REFERENCES mascota(mascota_id),
    FOREIGN KEY (medico_id) REFERENCES medico(id)
);

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

-- ---------------------------------------------------------
-- Punto 5 - Historia medica y anotaciones
-- ---------------------------------------------------------

CREATE TABLE historias_medicas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    mascota_id BIGINT NOT NULL,
    fecha_creacion DATETIME NOT NULL,
    observaciones_generales VARCHAR(255),
    FOREIGN KEY (mascota_id) REFERENCES mascota(mascota_id)
);

CREATE TABLE anotaciones_historia (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    historia_medica_id BIGINT NOT NULL,
    descripcion VARCHAR(500) NOT NULL,
    fecha DATETIME NOT NULL,
    FOREIGN KEY (historia_medica_id) REFERENCES historias_medicas(id)
);

-- ---------------------------------------------------------
-- Datos de prueba minimos (para poder probar los servicios
-- que dependen de mascota_id = 1 y medico_id = 1)
-- ---------------------------------------------------------

INSERT INTO cliente (tipo_documento, numero_documento, nombres, apellidos, fecha_nacimiento, genero, telefono, direccion, activo)
VALUES ('CC', '123456789', 'Juan', 'Perez', '1990-05-10', 'M', '3001234567', 'Calle 1 # 2-3', true);

INSERT INTO raza (nombre, especie, fecha_creacion, fecha_modificacion)
VALUES ('Labrador', 'Perro', NOW(), NOW());

INSERT INTO mascota (nombre_mascota, edad, fecha_registro, fecha_modificacion, raza_id, cliente_id)
VALUES ('Firulais', 3, NOW(), NOW(), 1, 1);

INSERT INTO especializacion (nombre, descripcion, codigo_especializacion)
VALUES ('Medicina General', 'Consulta general veterinaria', 'MG01');

INSERT INTO medico (tipo_documento, numero_documento, nombres, apellidos, telefono, registro_profesional, especializacion_id)
VALUES ('CC', '987654321', 'Laura', 'Gomez', '3109876543', 'RP-001', 1);
