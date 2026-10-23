-- =====================================================================
-- Segundo Parcial Programación Web - Clínica Veterinaria
-- Script completo de la base de datos "clinica" (estructura + datos de prueba).
--
-- ATENCIÓN: el script BORRA y vuelve a crear la base de datos "clinica".
-- Si tiene datos que quiera conservar, haga un respaldo antes de ejecutarlo.
--
-- Las fechas de las citas, historias y fórmulas se calculan a partir de la
-- fecha actual (CURDATE / NOW), para que siempre existan citas pasadas
-- (atendidas) y futuras (pendientes) con las que probar las reglas.
-- =====================================================================

DROP DATABASE IF EXISTS clinica;
CREATE DATABASE clinica CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE clinica;

-- ---------------------------------------------------------------------
-- Tablas base
-- ---------------------------------------------------------------------
CREATE TABLE especializacion (
    id                     BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre                 VARCHAR(100) NOT NULL,
    descripcion            TEXT,
    codigo_especializacion VARCHAR(10)  NOT NULL,
    CONSTRAINT uk_especializacion_codigo UNIQUE (codigo_especializacion)
);

CREATE TABLE medico (
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
    tipo_documento       VARCHAR(10)  NOT NULL,
    numero_documento     VARCHAR(20)  NOT NULL,
    nombres              VARCHAR(100) NOT NULL,
    apellidos            VARCHAR(100) NOT NULL,
    telefono             VARCHAR(20),
    registro_profesional VARCHAR(50)  NOT NULL,
    especializacion_id   BIGINT       NOT NULL,
    CONSTRAINT uk_medico_documento UNIQUE (tipo_documento, numero_documento),
    CONSTRAINT uk_medico_registro UNIQUE (registro_profesional),
    CONSTRAINT fk_medico_especializacion FOREIGN KEY (especializacion_id) REFERENCES especializacion (id)
);

CREATE TABLE raza (
    raza_id            INT AUTO_INCREMENT PRIMARY KEY,
    nombre             VARCHAR(100) NOT NULL,
    especie            VARCHAR(50)  NOT NULL,
    fecha_creacion     DATETIME,
    fecha_modificacion DATETIME
);

CREATE TABLE cliente (
    cliente_id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    tipo_documento   VARCHAR(10)  NOT NULL,
    numero_documento VARCHAR(20)  NOT NULL,
    nombres          VARCHAR(100) NOT NULL,
    apellidos        VARCHAR(100) NOT NULL,
    fecha_nacimiento DATE,
    genero           VARCHAR(20),
    telefono         VARCHAR(20),
    direccion        VARCHAR(255),
    activo           BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_cliente_documento UNIQUE (tipo_documento, numero_documento)
);

CREATE TABLE mascota (
    mascota_id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre_mascota     VARCHAR(100) NOT NULL,
    edad               INT,
    fecha_registro     DATETIME,
    fecha_modificacion DATETIME,
    raza_id            INT,
    cliente_id         BIGINT       NOT NULL,
    CONSTRAINT fk_mascota_raza    FOREIGN KEY (raza_id)    REFERENCES raza (raza_id),
    CONSTRAINT fk_mascota_cliente FOREIGN KEY (cliente_id) REFERENCES cliente (cliente_id)
);

CREATE TABLE usuario (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    username       VARCHAR(50)  NOT NULL,
    password_hash  TEXT,
    rol            VARCHAR(30)  NOT NULL,
    fecha_creacion DATETIME,
    activo         BOOLEAN,
    email          VARCHAR(255) NOT NULL,
    CONSTRAINT uk_usuario_username UNIQUE (username)
);

CREATE TABLE medicamento (
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre             VARCHAR(100) NOT NULL,
    presentacion       VARCHAR(80)  NOT NULL,
    descripcion        TEXT,
    fecha_creacion     DATETIME,
    fecha_modificacion DATETIME,
    CONSTRAINT uk_medicamento_nombre_presentacion UNIQUE (nombre, presentacion)
);

-- ---------------------------------------------------------------------
-- Tablas del parcial
-- ---------------------------------------------------------------------

-- Citas: el cliente se obtiene a través de la mascota (sin duplicar la relación).
CREATE TABLE cita (
    cita_id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    fecha_cita         DATETIME     NOT NULL,
    motivo             VARCHAR(255) NOT NULL,
    estado             VARCHAR(30)  NOT NULL,
    fecha_creacion     DATETIME,
    fecha_modificacion DATETIME,
    mascota_id         BIGINT       NOT NULL,
    medico_id          BIGINT       NOT NULL,
    CONSTRAINT ck_cita_estado CHECK (estado IN ('PENDIENTE', 'PROGRAMADA', 'CONFIRMADA', 'ATENDIDA', 'CANCELADA')),
    CONSTRAINT fk_cita_mascota FOREIGN KEY (mascota_id) REFERENCES mascota (mascota_id),
    CONSTRAINT fk_cita_medico  FOREIGN KEY (medico_id)  REFERENCES medico (id),
    INDEX idx_cita_medico_fecha (medico_id, fecha_cita),
    INDEX idx_cita_mascota_fecha (mascota_id, fecha_cita)
);

-- Historia clínica: una por cita atendida (cita_id es UNIQUE).
CREATE TABLE historia_medica (
    historia_id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    cita_id            BIGINT       NOT NULL,
    mascota_id         BIGINT       NOT NULL,
    medico_id          BIGINT       NOT NULL,
    fecha_consulta     DATETIME     NOT NULL,
    motivo_consulta    VARCHAR(255) NOT NULL,
    diagnostico        TEXT         NOT NULL,
    observaciones      TEXT,
    estado             VARCHAR(20)  NOT NULL,
    fecha_creacion     DATETIME,
    fecha_modificacion DATETIME,
    CONSTRAINT uk_historia_cita UNIQUE (cita_id),
    CONSTRAINT ck_historia_estado CHECK (estado IN ('ABIERTA', 'CERRADA')),
    CONSTRAINT fk_historia_cita    FOREIGN KEY (cita_id)    REFERENCES cita (cita_id),
    CONSTRAINT fk_historia_mascota FOREIGN KEY (mascota_id) REFERENCES mascota (mascota_id),
    CONSTRAINT fk_historia_medico  FOREIGN KEY (medico_id)  REFERENCES medico (id)
);

-- Anotaciones: N por historia clínica.
CREATE TABLE anotacion_historia (
    anotacion_id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    historia_id           BIGINT   NOT NULL,
    medico_id             BIGINT   NOT NULL,
    fecha_anotacion       DATETIME NOT NULL,
    descripcion           TEXT     NOT NULL,
    observacion           TEXT     NOT NULL,
    diagnostico_evolucion TEXT     NOT NULL,
    fecha_creacion        DATETIME,
    CONSTRAINT fk_anotacion_historia FOREIGN KEY (historia_id) REFERENCES historia_medica (historia_id),
    CONSTRAINT fk_anotacion_medico   FOREIGN KEY (medico_id)   REFERENCES medico (id)
);

-- Fórmulas: N por historia clínica. Nunca se borran; se inactivan (activo = FALSE).
CREATE TABLE formula_medica (
    formula_id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    mascota_id         BIGINT         NOT NULL,
    historia_id        BIGINT         NOT NULL,
    medico_id          BIGINT         NOT NULL,
    fecha_emision      DATE           NOT NULL,
    medicamento        VARCHAR(150)   NOT NULL,
    dosis              DECIMAL(10, 2) NOT NULL,
    unidad_dosis       VARCHAR(20)    NOT NULL,
    frecuencia_horas   INT            NOT NULL,
    duracion_dias      INT            NOT NULL,
    indicaciones       TEXT           NOT NULL,
    activo             BOOLEAN        NOT NULL DEFAULT TRUE,
    fecha_creacion     DATETIME,
    fecha_modificacion DATETIME,
    CONSTRAINT ck_formula_dosis      CHECK (dosis > 0),
    CONSTRAINT ck_formula_frecuencia CHECK (frecuencia_horas BETWEEN 1 AND 72),
    CONSTRAINT ck_formula_duracion   CHECK (duracion_dias BETWEEN 1 AND 365),
    CONSTRAINT fk_formula_mascota  FOREIGN KEY (mascota_id)  REFERENCES mascota (mascota_id),
    CONSTRAINT fk_formula_historia FOREIGN KEY (historia_id) REFERENCES historia_medica (historia_id),
    CONSTRAINT fk_formula_medico   FOREIGN KEY (medico_id)   REFERENCES medico (id)
);

-- ---------------------------------------------------------------------
-- Datos de prueba
-- ---------------------------------------------------------------------
INSERT INTO especializacion (id, nombre, descripcion, codigo_especializacion) VALUES
(1, 'Medicina general', 'Consulta general de pequeñas especies', 'MG'),
(2, 'Cirugía', 'Procedimientos quirúrgicos', 'CIR'),
(3, 'Dermatología', 'Enfermedades de la piel', 'DER');

INSERT INTO medico (id, tipo_documento, numero_documento, nombres, apellidos, telefono, registro_profesional, especializacion_id) VALUES
(1, 'CC', '80123456', 'Laura', 'Gómez', '3001112233', 'RP-1001', 1),
(2, 'CC', '80987654', 'Andrés', 'Martínez', '3002223344', 'RP-1002', 2),
(3, 'CC', '52345678', 'Camila', 'Rojas', '3003334455', 'RP-1003', 3),
(4, 'CC', '79456123', 'Felipe', 'Torres', '3004445566', 'RP-1004', 1);

INSERT INTO raza (raza_id, nombre, especie, fecha_creacion) VALUES
(1, 'Labrador', 'Perro', NOW()),
(2, 'Criollo', 'Perro', NOW()),
(3, 'Bulldog Francés', 'Perro', NOW()),
(4, 'Siamés', 'Gato', NOW()),
(5, 'Persa', 'Gato', NOW()),
(6, 'Criollo', 'Gato', NOW());

INSERT INTO cliente (cliente_id, tipo_documento, numero_documento, nombres, apellidos, fecha_nacimiento, genero, telefono, direccion, activo) VALUES
(1, 'CC', '1012345678', 'Juan', 'Pérez', '1990-04-12', 'Masculino', '3105556677', 'Calle 10 # 5-20', TRUE),
(2, 'CC', '1023456789', 'María', 'López', '1985-09-30', 'Femenino', '3116667788', 'Carrera 7 # 45-10', TRUE),
(3, 'CC', '1034567890', 'Carlos', 'Ramírez', '1978-01-15', 'Masculino', '3127778899', 'Avenida 68 # 12-30', TRUE),
(4, 'CE', '2045678901', 'Ana', 'Fernández', '1995-06-22', 'Femenino', '3138889900', 'Calle 80 # 20-15', TRUE),
(5, 'CC', '1056789012', 'Pedro', 'Castillo', '1970-11-03', 'Masculino', '3149990011', 'Calle 26 # 30-40', FALSE);

INSERT INTO mascota (mascota_id, nombre_mascota, edad, fecha_registro, raza_id, cliente_id) VALUES
(1, 'Max', 5, NOW(), 1, 1),
(2, 'Luna', 3, NOW(), 4, 1),
(3, 'Rocky', 7, NOW(), 2, 2),
(4, 'Michi', 2, NOW(), 6, 2),
(5, 'Toby', 4, NOW(), 3, 3),
(6, 'Nala', 6, NOW(), 5, 4),
(7, 'Simón', 1, NOW(), 2, 4),
(8, 'Kira', 9, NOW(), 1, 5);

-- Citas pasadas atendidas (1-6), cancelada (7), futuras activas (8-11) y una de hoy (12).
INSERT INTO cita (cita_id, fecha_cita, motivo, estado, fecha_creacion, mascota_id, medico_id) VALUES
(1,  TIMESTAMP(CURDATE() - INTERVAL 30 DAY, '09:00:00'), 'Vacunación anual',              'ATENDIDA',   NOW() - INTERVAL 35 DAY, 1, 1),
(2,  TIMESTAMP(CURDATE() - INTERVAL 20 DAY, '10:30:00'), 'Control de piel, rasquiña',     'ATENDIDA',   NOW() - INTERVAL 25 DAY, 2, 3),
(3,  TIMESTAMP(CURDATE() - INTERVAL 15 DAY, '15:00:00'), 'Cojera en pata trasera',        'ATENDIDA',   NOW() - INTERVAL 18 DAY, 3, 2),
(4,  TIMESTAMP(CURDATE() - INTERVAL 10 DAY, '08:30:00'), 'Vómito y decaimiento',          'ATENDIDA',   NOW() - INTERVAL 12 DAY, 1, 4),
(5,  TIMESTAMP(CURDATE() - INTERVAL 5 DAY,  '11:00:00'), 'Desparasitación',               'ATENDIDA',   NOW() - INTERVAL 7 DAY,  5, 1),
(6,  TIMESTAMP(CURDATE() - INTERVAL 2 DAY,  '16:00:00'), 'Revisión dental',               'ATENDIDA',   NOW() - INTERVAL 4 DAY,  6, 4),
(7,  TIMESTAMP(CURDATE() + INTERVAL 3 DAY,  '09:00:00'), 'Control general',               'CANCELADA',  NOW() - INTERVAL 1 DAY,  4, 1),
(8,  TIMESTAMP(CURDATE() + INTERVAL 1 DAY,  '09:00:00'), 'Control post vacunación',       'PENDIENTE',  NOW(), 1, 1),
(9,  TIMESTAMP(CURDATE() + INTERVAL 1 DAY,  '10:00:00'), 'Revisión de herida',            'PROGRAMADA', NOW(), 3, 2),
(10, TIMESTAMP(CURDATE() + INTERVAL 2 DAY,  '14:00:00'), 'Consulta dermatológica',        'CONFIRMADA', NOW(), 2, 3),
(11, TIMESTAMP(CURDATE() + INTERVAL 7 DAY,  '08:00:00'), 'Esterilización (valoración)',   'PENDIENTE',  NOW(), 7, 2),
(12, TIMESTAMP(CURDATE(), '07:00:00'),                   'Chequeo general',               'CONFIRMADA', NOW() - INTERVAL 2 DAY, 7, 1);

INSERT INTO historia_medica (historia_id, cita_id, mascota_id, medico_id, fecha_consulta, motivo_consulta, diagnostico, observaciones, estado, fecha_creacion) VALUES
(1, 1, 1, 1, TIMESTAMP(CURDATE() - INTERVAL 30 DAY, '09:10:00'), 'Vacunación anual', 'Paciente sano, se aplica vacuna múltiple.', 'Próximo refuerzo en un año.', 'CERRADA', NOW() - INTERVAL 30 DAY),
(2, 2, 2, 3, TIMESTAMP(CURDATE() - INTERVAL 20 DAY, '10:40:00'), 'Control de piel, rasquiña', 'Dermatitis alérgica leve.', 'Cambiar shampoo y revisar alimentación.', 'ABIERTA', NOW() - INTERVAL 20 DAY),
(3, 3, 3, 2, TIMESTAMP(CURDATE() - INTERVAL 15 DAY, '15:15:00'), 'Cojera en pata trasera', 'Esguince grado I en tarso izquierdo.', 'Reposo relativo por 10 días.', 'ABIERTA', NOW() - INTERVAL 15 DAY),
(4, 4, 1, 4, TIMESTAMP(CURDATE() - INTERVAL 10 DAY, '08:45:00'), 'Vómito y decaimiento', 'Gastroenteritis aguda.', 'Dieta blanda por 5 días.', 'ABIERTA', NOW() - INTERVAL 10 DAY);

INSERT INTO anotacion_historia (historia_id, medico_id, fecha_anotacion, descripcion, observacion, diagnostico_evolucion, fecha_creacion) VALUES
(1, 1, TIMESTAMP(CURDATE() - INTERVAL 30 DAY, '09:20:00'), 'Se aplica vacuna múltiple canina.', 'Sin reacción inmediata.', 'Paciente estable.', NOW() - INTERVAL 30 DAY),
(2, 3, TIMESTAMP(CURDATE() - INTERVAL 20 DAY, '11:00:00'), 'Raspado de piel y citología.', 'No se observan ácaros.', 'Compatible con dermatitis alérgica.', NOW() - INTERVAL 20 DAY),
(2, 3, TIMESTAMP(CURDATE() - INTERVAL 13 DAY, '10:00:00'), 'Control telefónico con la propietaria.', 'Disminuye la rasquiña.', 'Evolución favorable.', NOW() - INTERVAL 13 DAY),
(2, 3, TIMESTAMP(CURDATE() - INTERVAL 6 DAY, '10:30:00'), 'Revisión presencial de la piel.', 'Piel sin lesiones activas.', 'Dermatitis en remisión.', NOW() - INTERVAL 6 DAY),
(3, 2, TIMESTAMP(CURDATE() - INTERVAL 15 DAY, '15:40:00'), 'Radiografía de miembro posterior.', 'Sin fracturas.', 'Esguince confirmado.', NOW() - INTERVAL 15 DAY),
(3, 2, TIMESTAMP(CURDATE() - INTERVAL 8 DAY, '16:00:00'), 'Control de cojera.', 'Apoya la pata con leve molestia.', 'Mejoría parcial.', NOW() - INTERVAL 8 DAY),
(4, 4, TIMESTAMP(CURDATE() - INTERVAL 10 DAY, '09:00:00'), 'Examen físico y palpación abdominal.', 'Dolor abdominal leve.', 'Gastroenteritis aguda.', NOW() - INTERVAL 10 DAY),
(4, 4, TIMESTAMP(CURDATE() - INTERVAL 7 DAY, '09:00:00'), 'Control de hidratación.', 'Come dieta blanda sin vomitar.', 'Evolución favorable.', NOW() - INTERVAL 7 DAY);

INSERT INTO formula_medica (mascota_id, historia_id, medico_id, fecha_emision, medicamento, dosis, unidad_dosis, frecuencia_horas, duracion_dias, indicaciones, activo, fecha_creacion) VALUES
(2, 2, 3, CURDATE() - INTERVAL 20 DAY, 'Prednisolona', 5.00, 'mg', 24, 7, 'Dar con comida en la mañana. No suspender de forma brusca.', TRUE,  NOW() - INTERVAL 20 DAY),
(2, 2, 3, CURDATE() - INTERVAL 20 DAY, 'Shampoo de clorhexidina', 10.00, 'ml', 72, 30, 'Bañar cada 3 días dejando actuar 10 minutos.', TRUE, NOW() - INTERVAL 20 DAY + INTERVAL 5 MINUTE),
(3, 3, 2, CURDATE() - INTERVAL 15 DAY, 'Meloxicam', 1.50, 'mg', 24, 5, 'Administrar después de comer.', TRUE, NOW() - INTERVAL 15 DAY),
(3, 3, 2, CURDATE() - INTERVAL 15 DAY, 'Tramadol', 2.00, 'mg', 12, 3, 'Solo si hay dolor fuerte.', FALSE, NOW() - INTERVAL 15 DAY + INTERVAL 5 MINUTE),
(1, 4, 4, CURDATE() - INTERVAL 10 DAY, 'Metronidazol', 250.00, 'mg', 12, 5, 'Dar con comida. Completar el tratamiento.', TRUE, NOW() - INTERVAL 10 DAY),
(1, 4, 4, CURDATE() - INTERVAL 10 DAY, 'Suero oral', 50.00, 'ml', 8, 3, 'Ofrecer en pequeñas cantidades.', TRUE, NOW() - INTERVAL 10 DAY + INTERVAL 5 MINUTE);
