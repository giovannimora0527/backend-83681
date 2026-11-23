CREATE TABLE importacion_inventario (
    id BIGINT NOT NULL AUTO_INCREMENT,
    archivo VARCHAR(255) NOT NULL,
    sha256 CHAR(64) NOT NULL,
    usuario_id BIGINT NOT NULL,
    total_filas INT NOT NULL,
    filas_importadas INT NOT NULL,
    filas_rechazadas INT NOT NULL,
    fecha DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    KEY ix_importacion_usuario_fecha (usuario_id, fecha),
    CONSTRAINT fk_importacion_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id)
) ENGINE=InnoDB;

CREATE TABLE importacion_inventario_detalle (
    id BIGINT NOT NULL AUTO_INCREMENT,
    importacion_id BIGINT NOT NULL,
    numero_fila INT NOT NULL,
    codigo_producto VARCHAR(8) NULL,
    codigo_lote VARCHAR(80) NULL,
    estado VARCHAR(20) NOT NULL,
    mensaje VARCHAR(500) NULL,
    PRIMARY KEY (id),
    KEY ix_importacion_detalle_lote (importacion_id, numero_fila),
    CONSTRAINT fk_importacion_detalle FOREIGN KEY (importacion_id) REFERENCES importacion_inventario (id)
) ENGINE=InnoDB;
