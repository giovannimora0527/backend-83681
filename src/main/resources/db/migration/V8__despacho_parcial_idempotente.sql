CREATE TABLE despacho_inventario (
    id BIGINT NOT NULL AUTO_INCREMENT,
    formula_id BIGINT NOT NULL,
    usuario_id BIGINT NOT NULL,
    clave_idempotencia VARCHAR(64) NOT NULL,
    cantidad_solicitada INT NOT NULL,
    cantidad_despachada INT NOT NULL,
    fecha DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_despacho_clave (clave_idempotencia),
    KEY ix_despacho_formula_fecha (formula_id, fecha),
    CONSTRAINT ck_despacho_solicitada CHECK (cantidad_solicitada > 0),
    CONSTRAINT ck_despacho_real CHECK (cantidad_despachada > 0 AND cantidad_despachada <= cantidad_solicitada),
    CONSTRAINT fk_despacho_inv_formula FOREIGN KEY (formula_id) REFERENCES formula_medica (id),
    CONSTRAINT fk_despacho_inv_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id)
) ENGINE=InnoDB;

ALTER TABLE movimiento_inventario
    ADD COLUMN despacho_id BIGINT NULL,
    ADD KEY ix_movimiento_despacho (despacho_id),
    ADD CONSTRAINT fk_movimiento_despacho FOREIGN KEY (despacho_id) REFERENCES despacho_inventario (id);
