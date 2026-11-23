ALTER TABLE formula_medica
    ADD COLUMN cantidad_prescrita INT NOT NULL DEFAULT 1,
    ADD COLUMN estado_dispensacion VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0,
    ADD CONSTRAINT ck_formula_cantidad_prescrita CHECK (cantidad_prescrita > 0);

