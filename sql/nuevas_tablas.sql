-- =========================================================
-- Tablas nuevas requeridas por el Primer Parcial de
-- Programacion Web (NRC 83861) sobre la base de datos "clinica".
-- Ejecutar despues de tener creadas: cliente, mascota, medico,
-- raza, especializacion, usuario.
-- =========================================================

-- Parte 1: Formulas medicas del inventario
CREATE TABLE IF NOT EXISTS formula_medica (
    formula_id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    medicamento         VARCHAR(150) NOT NULL,
    dosis               VARCHAR(100) NOT NULL,
    indicaciones        TEXT,
    cantidad            INT NOT NULL,
    fecha_creacion      DATETIME,
    fecha_modificacion  DATETIME,
    mascota_id          BIGINT,
    medico_id           BIGINT,
    CONSTRAINT fk_formula_mascota FOREIGN KEY (mascota_id) REFERENCES mascota (mascota_id),
    CONSTRAINT fk_formula_medico  FOREIGN KEY (medico_id)  REFERENCES medico (id)
);

-- Parte 2: Citas
CREATE TABLE IF NOT EXISTS cita (
    cita_id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    fecha_cita          DATETIME NOT NULL,
    motivo              VARCHAR(255) NOT NULL,
    estado              VARCHAR(30) NOT NULL,
    fecha_creacion      DATETIME,
    fecha_modificacion  DATETIME,
    mascota_id          BIGINT NOT NULL,
    medico_id           BIGINT NOT NULL,
    CONSTRAINT fk_cita_mascota FOREIGN KEY (mascota_id) REFERENCES mascota (mascota_id),
    CONSTRAINT fk_cita_medico  FOREIGN KEY (medico_id)  REFERENCES medico (id)
);

-- Parte 3: Historia medica
CREATE TABLE IF NOT EXISTS historia_medica (
    historia_id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    diagnostico         TEXT,
    tratamiento         TEXT,
    fecha_creacion      DATETIME,
    fecha_modificacion  DATETIME,
    mascota_id          BIGINT NOT NULL,
    medico_id           BIGINT NOT NULL,
    CONSTRAINT fk_historia_mascota FOREIGN KEY (mascota_id) REFERENCES mascota (mascota_id),
    CONSTRAINT fk_historia_medico  FOREIGN KEY (medico_id)  REFERENCES medico (id)
);

-- Parte 4: Anotacion de historia (relacionada con historia_medica)
CREATE TABLE IF NOT EXISTS anotacion_historia (
    anotacion_id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    observacion         TEXT NOT NULL,
    fecha_creacion      DATETIME,
    fecha_modificacion  DATETIME,
    historia_id         BIGINT NOT NULL,
    medico_id           BIGINT NOT NULL,
    CONSTRAINT fk_anotacion_historia FOREIGN KEY (historia_id) REFERENCES historia_medica (historia_id),
    CONSTRAINT fk_anotacion_medico   FOREIGN KEY (medico_id)   REFERENCES medico (id)
);
