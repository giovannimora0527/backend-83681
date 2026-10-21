-- Ejecutar una sola vez sobre la base de datos clinica existente.
ALTER TABLE formula_medica ADD COLUMN frecuencia VARCHAR(150) NULL;
ALTER TABLE formula_medica ADD COLUMN duracion_tratamiento VARCHAR(100) NULL;
ALTER TABLE historia_medica ADD COLUMN cita_id BIGINT NULL;
ALTER TABLE historia_medica ADD COLUMN medico_id BIGINT NULL;
ALTER TABLE historia_medica ADD COLUMN motivo TEXT NULL;
ALTER TABLE historia_medica ADD COLUMN diagnostico TEXT NULL;
ALTER TABLE historia_medica ADD COLUMN observaciones TEXT NULL;
ALTER TABLE anotacion_historia ADD COLUMN diagnostico TEXT NULL;
ALTER TABLE anotacion_historia ADD COLUMN observacion TEXT NULL;

ALTER TABLE historia_medica ADD CONSTRAINT fk_historia_cita FOREIGN KEY (cita_id) REFERENCES cita(id);
ALTER TABLE historia_medica ADD CONSTRAINT fk_historia_medico FOREIGN KEY (medico_id) REFERENCES medico(id);
