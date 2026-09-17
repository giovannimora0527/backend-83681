-- ============================================================================
-- Script de creacion de las tablas requeridas en el primer parcial.
-- Base de datos: clinica (MySQL)
-- Ajustar los nombres de columna si la base de datos entregada en clase
-- usa otros nombres; en ese caso se debe ajustar tambien el @Column de la
-- entidad correspondiente.
-- ============================================================================

-- Requerimiento 1: formulas medicas del inventario
CREATE TABLE IF NOT EXISTS formula_medica (
    formula_id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    medicamento         VARCHAR(150) NOT NULL,
    dosis               VARCHAR(100),
    cantidad            INT,
    observaciones       TEXT,
    fecha_creacion      DATETIME,
    fecha_modificacion  DATETIME,
    mascota_id          BIGINT,
    medico_id           BIGINT,
    CONSTRAINT fk_formula_mascota FOREIGN KEY (mascota_id) REFERENCES mascota (mascota_id),
    CONSTRAINT fk_formula_medico  FOREIGN KEY (medico_id)  REFERENCES medico (id)
);

-- Requerimientos 2, 3 y 4: citas
CREATE TABLE IF NOT EXISTS cita (
    cita_id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    fecha_cita          DATETIME NOT NULL,
    motivo              VARCHAR(250),
    estado              VARCHAR(20),
    fecha_creacion      DATETIME,
    fecha_modificacion  DATETIME,
    mascota_id          BIGINT,
    medico_id           BIGINT,
    CONSTRAINT fk_cita_mascota FOREIGN KEY (mascota_id) REFERENCES mascota (mascota_id),
    CONSTRAINT fk_cita_medico  FOREIGN KEY (medico_id)  REFERENCES medico (id)
);

-- Requerimiento 5: historia medica
CREATE TABLE IF NOT EXISTS historia_medica (
    historia_id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    diagnostico         TEXT,
    tratamiento         TEXT,
    fecha_creacion      DATETIME,
    fecha_modificacion  DATETIME,
    mascota_id          BIGINT,
    CONSTRAINT fk_historia_mascota FOREIGN KEY (mascota_id) REFERENCES mascota (mascota_id)
);

-- Requerimiento 5: anotaciones de la historia medica (relacion 1 a N)
CREATE TABLE IF NOT EXISTS anotacion_historia (
    anotacion_id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    descripcion         TEXT,
    fecha_creacion      DATETIME,
    fecha_modificacion  DATETIME,
    historia_id         BIGINT,
    CONSTRAINT fk_anotacion_historia FOREIGN KEY (historia_id) REFERENCES historia_medica (historia_id)
);
