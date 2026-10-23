-- =====================================================================
-- SEGUNDO PARCIAL - Script de ajuste de la base de datos "clinica"
-- =====================================================================
-- Ajusta las tablas cita, historia_medica, anotacion_historia y
-- formula_medica para soportar las reglas de negocio del parcial.
-- Los datos existentes se conservan y se adaptan a las nuevas columnas.
-- Ejecutar UNA sola vez sobre la base de datos "clinica".
-- =====================================================================

USE clinica;

-- ---------------------------------------------------------------------
-- 1. CITA
-- Se normalizan los estados a los 5 que exige el parcial:
-- PENDIENTE, PROGRAMADA, CONFIRMADA, ATENDIDA, CANCELADA.
-- ---------------------------------------------------------------------
UPDATE cita SET estado = 'ATENDIDA'   WHERE estado = 'Completada';
UPDATE cita SET estado = 'PROGRAMADA' WHERE estado IN ('Programada', 'Reprogramada');
UPDATE cita SET estado = 'PENDIENTE'  WHERE estado IS NULL OR estado = '';

ALTER TABLE cita
    MODIFY estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE';

-- ---------------------------------------------------------------------
-- 2. HISTORIA MEDICA (historia clinica / consulta)
-- Cada historia queda asociada a UNA cita atendida (relacion 1 a 1),
-- al veterinario que atendio, con motivo, diagnostico y estado.
-- ---------------------------------------------------------------------
ALTER TABLE historia_medica
    RENAME COLUMN fecha_apertura TO fecha_consulta,
    ADD COLUMN cita_id     BIGINT       NULL AFTER mascota_id,
    ADD COLUMN medico_id   BIGINT       NULL AFTER cita_id,
    ADD COLUMN motivo      VARCHAR(200) NULL AFTER fecha_consulta,
    ADD COLUMN diagnostico TEXT         NULL AFTER motivo,
    ADD COLUMN estado      VARCHAR(20)  NOT NULL DEFAULT 'ABIERTA';

-- Las historias que ya existian se asocian a la cita atendida de su mascota.
UPDATE historia_medica h
    JOIN cita c ON c.cita_id = (
        SELECT MIN(c2.cita_id) FROM cita c2
        WHERE c2.mascota_id = h.mascota_id AND c2.estado = 'ATENDIDA')
SET h.cita_id        = c.cita_id,
    h.medico_id      = c.medico_id,
    h.fecha_consulta = c.fecha_cita,
    h.motivo         = c.motivo,
    h.diagnostico    = 'Registro anterior al sistema (sin diagnostico)';

ALTER TABLE historia_medica
    MODIFY cita_id   BIGINT NOT NULL,
    MODIFY medico_id BIGINT NOT NULL,
    ADD CONSTRAINT uk_historia_cita UNIQUE (cita_id),
    ADD CONSTRAINT fk_historia_cita   FOREIGN KEY (cita_id)   REFERENCES cita (cita_id),
    ADD CONSTRAINT fk_historia_medico FOREIGN KEY (medico_id) REFERENCES medico (id);

-- ---------------------------------------------------------------------
-- 3. ANOTACION HISTORIA
-- Se agregan observacion y diagnostico/evolucion.
-- ---------------------------------------------------------------------
ALTER TABLE anotacion_historia
    ADD COLUMN observacion TEXT NULL AFTER descripcion,
    ADD COLUMN diagnostico TEXT NULL AFTER observacion;

-- ---------------------------------------------------------------------
-- 4. FORMULA MEDICA
-- - Se asocia a la cita (consulta) en la que se emitio.
-- - La dosis se separa en cantidad numerica + unidad (para validarla).
-- - La duracion pasa a ser un numero de dias (para validarla).
-- - Se agrega fecha de emision, fecha de modificacion y "activo"
--   (inactivacion en lugar de borrado fisico).
-- ---------------------------------------------------------------------
ALTER TABLE formula_medica
    RENAME COLUMN observaciones TO indicaciones,
    ADD COLUMN cita_id            BIGINT        NULL AFTER medico_id,
    ADD COLUMN dosis_cantidad     DECIMAL(10,2) NULL AFTER medicamento,
    ADD COLUMN dosis_unidad       VARCHAR(20)   NULL AFTER dosis_cantidad,
    ADD COLUMN duracion_dias      INT           NULL AFTER frecuencia,
    ADD COLUMN fecha_emision      DATETIME      NULL,
    ADD COLUMN fecha_modificacion DATETIME      NULL,
    ADD COLUMN activo             TINYINT(1)    NOT NULL DEFAULT 1;

-- Se convierten los textos antiguos ("250 mg", "Durante 7 dias") a numeros.
UPDATE formula_medica
SET dosis_cantidad = CAST(SUBSTRING_INDEX(TRIM(dosis), ' ', 1) AS DECIMAL(10,2)),
    dosis_unidad   = TRIM(SUBSTRING(TRIM(dosis), LENGTH(SUBSTRING_INDEX(TRIM(dosis), ' ', 1)) + 1)),
    duracion_dias  = CAST(REGEXP_SUBSTR(duracion, '[0-9]+') AS UNSIGNED);

-- Las formulas existentes se asocian a la primera cita de su mascota.
UPDATE formula_medica f
    JOIN cita c ON c.cita_id = (
        SELECT MIN(c2.cita_id) FROM cita c2 WHERE c2.mascota_id = f.mascota_id)
SET f.cita_id       = c.cita_id,
    f.fecha_emision = GREATEST(f.fecha_creacion, c.fecha_cita);

ALTER TABLE formula_medica
    DROP COLUMN dosis,
    DROP COLUMN duracion,
    MODIFY cita_id        BIGINT        NOT NULL,
    MODIFY dosis_cantidad DECIMAL(10,2) NOT NULL,
    MODIFY dosis_unidad   VARCHAR(20)   NOT NULL,
    MODIFY frecuencia     VARCHAR(100)  NOT NULL,
    MODIFY duracion_dias  INT           NOT NULL,
    MODIFY fecha_emision  DATETIME      NOT NULL,
    ADD CONSTRAINT fk_formula_cita FOREIGN KEY (cita_id) REFERENCES cita (cita_id);
