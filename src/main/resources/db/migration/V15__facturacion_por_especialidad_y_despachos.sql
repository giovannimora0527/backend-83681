ALTER TABLE tarifa_servicio
    ADD COLUMN especializacion_id BIGINT NULL,
    ADD COLUMN medico_id BIGINT NULL,
    ADD KEY ix_tarifa_especializacion_vigencia (especializacion_id, activo, vigencia_desde, vigencia_hasta),
    ADD KEY ix_tarifa_medico_vigencia (medico_id, activo, vigencia_desde, vigencia_hasta),
    ADD CONSTRAINT fk_tarifa_especializacion FOREIGN KEY (especializacion_id) REFERENCES especializacion (id),
    ADD CONSTRAINT fk_tarifa_medico FOREIGN KEY (medico_id) REFERENCES medico (id);

ALTER TABLE factura_detalle
    ADD COLUMN despacho_id BIGINT NULL,
    ADD UNIQUE KEY uk_factura_detalle_despacho (despacho_id),
    ADD CONSTRAINT fk_factura_detalle_despacho FOREIGN KEY (despacho_id) REFERENCES despacho_inventario (id);

-- Keep legacy generic tariffs for old invoices, but prevent selecting them for new bills.
UPDATE tarifa_servicio
SET activo = b'0'
WHERE especializacion_id IS NULL AND medico_id IS NULL;

-- Sample consultation rates in COP, based on published Colombian clinic tariffs.
-- Doctor-specific rows can override these specialty defaults.
INSERT INTO tarifa_servicio
    (nombre, valor, impuesto_porcentaje, vigencia_desde, vigencia_hasta, activo, especializacion_id, medico_id)
SELECT
    CONCAT('Consulta - ', e.nombre),
    CASE
        WHEN LOWER(e.nombre) LIKE '%general%'
          OR LOWER(e.nombre) LIKE '%psiquiatr%'
          OR LOWER(e.nombre) LIKE '%psicolog%'
            THEN 94000.00
        WHEN LOWER(e.nombre) LIKE '%cardiolog%'
          OR LOWER(e.nombre) LIKE '%neurolog%'
          OR LOWER(e.nombre) LIKE '%oncolog%'
          OR LOWER(e.nombre) LIKE '%ortoped%'
          OR LOWER(e.nombre) LIKE '%traumatolog%'
          OR LOWER(e.nombre) LIKE '%anestesi%'
          OR LOWER(e.nombre) LIKE '%cirug%'
            THEN 200000.00
        ELSE 172500.00
    END,
    0.00,
    CURRENT_DATE(),
    NULL,
    b'1',
    e.id,
    NULL
FROM especializacion e
WHERE NOT EXISTS (
    SELECT 1 FROM tarifa_servicio t
    WHERE t.especializacion_id = e.id AND t.medico_id IS NULL AND t.activo = b'1'
);
