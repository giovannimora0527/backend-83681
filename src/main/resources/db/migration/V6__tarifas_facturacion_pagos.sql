CREATE TABLE tarifa_servicio (
    id BIGINT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(120) NOT NULL,
    valor DECIMAL(12,2) NOT NULL,
    impuesto_porcentaje DECIMAL(5,2) NOT NULL DEFAULT 0.00,
    vigencia_desde DATE NOT NULL,
    vigencia_hasta DATE NULL,
    activo BIT NOT NULL DEFAULT b'1',
    PRIMARY KEY (id),
    KEY ix_tarifa_vigencia (activo, vigencia_desde, vigencia_hasta),
    CONSTRAINT ck_tarifa_valor CHECK (valor >= 0),
    CONSTRAINT ck_tarifa_impuesto CHECK (impuesto_porcentaje BETWEEN 0 AND 100),
    CONSTRAINT ck_tarifa_fechas CHECK (vigencia_hasta IS NULL OR vigencia_hasta >= vigencia_desde)
) ENGINE=InnoDB;

CREATE TABLE factura (
    id BIGINT NOT NULL AUTO_INCREMENT,
    cita_id BIGINT NOT NULL,
    usuario_creacion_id BIGINT NOT NULL,
    subtotal DECIMAL(12,2) NOT NULL,
    descuento DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    impuesto DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    total DECIMAL(12,2) NOT NULL,
    estado VARCHAR(20) NOT NULL,
    fecha_creacion DATETIME(6) NOT NULL,
    fecha_anulacion DATETIME(6) NULL,
    usuario_anulacion_id BIGINT NULL,
    motivo_anulacion VARCHAR(500) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_factura_cita (cita_id),
    KEY ix_factura_fecha_estado (fecha_creacion, estado),
    CONSTRAINT ck_factura_montos CHECK (subtotal >= 0 AND descuento >= 0 AND impuesto >= 0 AND total >= 0),
    CONSTRAINT fk_factura_cita FOREIGN KEY (cita_id) REFERENCES cita (id),
    CONSTRAINT fk_factura_usuario FOREIGN KEY (usuario_creacion_id) REFERENCES usuario (id),
    CONSTRAINT fk_factura_usuario_anula FOREIGN KEY (usuario_anulacion_id) REFERENCES usuario (id)
) ENGINE=InnoDB;

CREATE TABLE factura_detalle (
    id BIGINT NOT NULL AUTO_INCREMENT,
    factura_id BIGINT NOT NULL,
    tarifa_id BIGINT NULL,
    descripcion VARCHAR(255) NOT NULL,
    cantidad INT NOT NULL,
    valor_unitario DECIMAL(12,2) NOT NULL,
    impuesto_porcentaje DECIMAL(5,2) NOT NULL,
    descuento DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    total_linea DECIMAL(12,2) NOT NULL,
    PRIMARY KEY (id),
    KEY ix_factura_detalle_factura (factura_id),
    CONSTRAINT ck_factura_detalle_cantidad CHECK (cantidad > 0),
    CONSTRAINT ck_factura_detalle_montos CHECK (valor_unitario >= 0 AND descuento >= 0 AND total_linea >= 0),
    CONSTRAINT fk_factura_detalle_factura FOREIGN KEY (factura_id) REFERENCES factura (id),
    CONSTRAINT fk_factura_detalle_tarifa FOREIGN KEY (tarifa_id) REFERENCES tarifa_servicio (id)
) ENGINE=InnoDB;

CREATE TABLE pago_factura (
    id BIGINT NOT NULL AUTO_INCREMENT,
    factura_id BIGINT NOT NULL,
    usuario_id BIGINT NOT NULL,
    monto DECIMAL(12,2) NOT NULL,
    metodo VARCHAR(30) NOT NULL,
    referencia VARCHAR(120) NULL,
    fecha DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    KEY ix_pago_factura_fecha (factura_id, fecha),
    CONSTRAINT ck_pago_monto CHECK (monto > 0),
    CONSTRAINT fk_pago_factura FOREIGN KEY (factura_id) REFERENCES factura (id),
    CONSTRAINT fk_pago_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id)
) ENGINE=InnoDB;
