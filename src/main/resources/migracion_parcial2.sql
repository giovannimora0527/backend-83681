-- =========================================================
-- MIGRACION para quien YA tiene la base del primer parcial.
-- Agrega las columnas que necesita el segundo parcial.
-- Ejecutar UNA sola vez (si una columna ya existe MySQL avisara del error).
-- Despues ejecute datos_prueba_parcial2.sql para cargar datos de ejemplo.
-- =========================================================
USE clinica;

-- Citas: estado
ALTER TABLE citas
    ADD COLUMN estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE';

-- Historias clinicas: ahora nacen de una cita atendida y guardan los datos de la consulta
ALTER TABLE historias_medicas
    ADD COLUMN cita_id BIGINT NULL,
    ADD COLUMN medico_id BIGINT NULL,
    ADD COLUMN fecha_consulta DATETIME NULL,
    ADD COLUMN motivo_consulta VARCHAR(200) NULL,
    ADD COLUMN diagnostico VARCHAR(500) NULL,
    ADD COLUMN estado VARCHAR(20) NOT NULL DEFAULT 'ABIERTA',
    MODIFY COLUMN observaciones_generales VARCHAR(500) NULL,
    ADD CONSTRAINT fk_historia_cita FOREIGN KEY (cita_id) REFERENCES citas(id),
    ADD CONSTRAINT fk_historia_medico FOREIGN KEY (medico_id) REFERENCES medico(id),
    ADD CONSTRAINT uq_historia_cita UNIQUE (cita_id);

-- Anotaciones: veterinario, observacion y diagnostico/evolucion
ALTER TABLE anotaciones_historia
    ADD COLUMN medico_id BIGINT NULL,
    ADD COLUMN observacion VARCHAR(500) NULL,
    ADD COLUMN diagnostico_evolucion VARCHAR(500) NULL,
    ADD CONSTRAINT fk_anotacion_medico FOREIGN KEY (medico_id) REFERENCES medico(id);

-- Formulas: ahora van ligadas a una historia clinica y llevan un unico medicamento con su pauta
ALTER TABLE formula_medica
    ADD COLUMN historia_medica_id BIGINT NULL,
    ADD COLUMN medicamento_id BIGINT NULL,
    ADD COLUMN fecha_emision DATETIME NULL,
    ADD COLUMN dosis DECIMAL(10,2) NULL,
    ADD COLUMN unidad_dosis VARCHAR(20) NULL,
    ADD COLUMN frecuencia_horas INT NULL,
    ADD COLUMN duracion_dias INT NULL,
    ADD COLUMN indicaciones VARCHAR(500) NULL,
    ADD COLUMN estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVA',
    ADD CONSTRAINT fk_formula_historia FOREIGN KEY (historia_medica_id) REFERENCES historias_medicas(id),
    ADD CONSTRAINT fk_formula_medicamento FOREIGN KEY (medicamento_id) REFERENCES medicamento(medicamento_id);

-- Indices para las validaciones de disponibilidad de citas
CREATE INDEX idx_citas_medico_fecha ON citas (medico_id, fecha);
CREATE INDEX idx_citas_mascota_fecha ON citas (mascota_id, fecha);
