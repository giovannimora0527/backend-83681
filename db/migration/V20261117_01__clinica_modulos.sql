-- Migracion aditiva para MySQL 8. No elimina ni reemplaza datos existentes.
-- Antes de aplicar, respaldar la base y revisar tablas de igual nombre.

ALTER TABLE medicamento
    ADD COLUMN codigo VARCHAR(8) NULL,
    ADD COLUMN tipo_producto VARCHAR(20) NOT NULL DEFAULT 'MEDICAMENTO',
    ADD COLUMN registro_invima VARCHAR(80) NULL,
    ADD COLUMN precio_compra DECIMAL(12,2) NOT NULL DEFAULT 0,
    ADD COLUMN precio_venta DECIMAL(12,2) NOT NULL DEFAULT 0,
    ADD COLUMN stock_minimo DECIMAL(12,3) NOT NULL DEFAULT 0,
    ADD COLUMN fecha_proximo_pedido DATE NULL,
    ADD COLUMN activo TINYINT(1) NOT NULL DEFAULT 1;
ALTER TABLE medicamento MODIFY COLUMN fecha_compra DATE NULL, MODIFY COLUMN fecha_vence DATE NULL;

UPDATE medicamento SET codigo = CONCAT('M', LPAD(id, 7, '0')) WHERE codigo IS NULL;
CREATE UNIQUE INDEX uk_medicamento_codigo ON medicamento(codigo);

ALTER TABLE cita
    ADD COLUMN fecha_fin DATETIME NULL,
    ADD COLUMN fecha_actualizacion DATETIME NULL;
UPDATE cita SET fecha_fin = DATE_ADD(fecha_hora, INTERVAL 30 MINUTE) WHERE fecha_fin IS NULL;
CREATE INDEX idx_cita_medico_rango ON cita(medico_id, fecha_hora, fecha_fin, estado);
CREATE INDEX idx_cita_mascota_rango ON cita(mascota_id, fecha_hora, fecha_fin, estado);

ALTER TABLE especializacion ADD COLUMN activo TINYINT(1) NOT NULL DEFAULT 1;

CREATE TABLE IF NOT EXISTS inventario_lote (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  medicamento_id BIGINT UNSIGNED NOT NULL,
  numero_lote VARCHAR(80) NOT NULL,
  fecha_ingreso DATE NOT NULL,
  fecha_vencimiento DATE NOT NULL,
  cantidad_inicial DECIMAL(12,3) NOT NULL,
  cantidad_disponible DECIMAL(12,3) NOT NULL,
  precio_compra_unitario DECIMAL(12,2) NOT NULL,
  precio_venta_unitario DECIMAL(12,2) NOT NULL,
  referencia_proveedor VARCHAR(120) NULL,
  creado_por BIGINT UNSIGNED NULL,
  creado_en DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id), UNIQUE KEY uk_lote_producto (medicamento_id, numero_lote),
  KEY idx_lote_fefo (medicamento_id, fecha_vencimiento, cantidad_disponible),
  CONSTRAINT fk_lote_medicamento FOREIGN KEY (medicamento_id) REFERENCES medicamento(id),
  CONSTRAINT fk_lote_usuario FOREIGN KEY (creado_por) REFERENCES usuario(id),
  CONSTRAINT chk_lote_cantidades CHECK (cantidad_inicial >= 0 AND cantidad_disponible >= 0 AND cantidad_disponible <= cantidad_inicial),
  CONSTRAINT chk_lote_precios CHECK (precio_compra_unitario >= 0 AND precio_venta_unitario >= 0)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS inventario_movimiento (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  lote_id BIGINT UNSIGNED NOT NULL,
  tipo ENUM('ENTRADA','SALIDA','AJUSTE_ENTRADA','AJUSTE_SALIDA','REVERSO') NOT NULL,
  cantidad DECIMAL(12,3) NOT NULL,
  referencia_tipo VARCHAR(40) NULL,
  referencia_id BIGINT UNSIGNED NULL,
  motivo VARCHAR(500) NULL,
  usuario_id BIGINT UNSIGNED NULL,
  creado_en DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id), KEY idx_mov_lote_fecha (lote_id, creado_en), KEY idx_mov_referencia (referencia_tipo, referencia_id),
  CONSTRAINT fk_mov_lote FOREIGN KEY (lote_id) REFERENCES inventario_lote(id),
  CONSTRAINT fk_mov_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id),
  CONSTRAINT chk_mov_cantidad CHECK (cantidad > 0)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS inventario_importacion (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT, archivo VARCHAR(255) NOT NULL,
  usuario_id BIGINT UNSIGNED NULL, total_filas INT NOT NULL DEFAULT 0,
  validas INT NOT NULL DEFAULT 0, invalidas INT NOT NULL DEFAULT 0,
  estado ENUM('PREVALIDADA','CONFIRMADA','FALLIDA') NOT NULL DEFAULT 'PREVALIDADA',
  resultado_json JSON NULL, creado_en DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  confirmado_en DATETIME NULL, PRIMARY KEY (id), KEY idx_import_usuario_fecha (usuario_id, creado_en),
  CONSTRAINT fk_import_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS cita_disponibilidad (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT, medico_id BIGINT UNSIGNED NOT NULL,
  dia_semana TINYINT UNSIGNED NOT NULL, hora_inicio TIME NOT NULL, hora_fin TIME NOT NULL,
  activo TINYINT(1) NOT NULL DEFAULT 1, PRIMARY KEY(id), KEY idx_disponibilidad_medico (medico_id, dia_semana, activo),
  CONSTRAINT fk_disponibilidad_medico FOREIGN KEY (medico_id) REFERENCES medico(id),
  CONSTRAINT chk_disponibilidad_dia CHECK (dia_semana BETWEEN 1 AND 7),
  CONSTRAINT chk_disponibilidad_hora CHECK (hora_inicio < hora_fin)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS cita_historial (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT, cita_id BIGINT UNSIGNED NOT NULL,
  estado_anterior VARCHAR(20) NULL, estado_nuevo VARCHAR(20) NOT NULL,
  nota VARCHAR(500) NULL, usuario_id BIGINT UNSIGNED NULL, creado_en DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY(id), KEY idx_cita_historial (cita_id, creado_en),
  CONSTRAINT fk_historial_cita FOREIGN KEY (cita_id) REFERENCES cita(id),
  CONSTRAINT fk_historial_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS atencion_medica (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT, cita_id BIGINT UNSIGNED NOT NULL,
  mascota_id INT NOT NULL, medico_id BIGINT UNSIGNED NOT NULL,
  motivo TEXT NULL, sintomas TEXT NULL, diagnostico TEXT NULL, observaciones TEXT NULL, recomendaciones TEXT NULL,
  atendida_en DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY(id), UNIQUE KEY uk_atencion_cita (cita_id),
  CONSTRAINT fk_atencion_cita FOREIGN KEY (cita_id) REFERENCES cita(id),
  CONSTRAINT fk_atencion_mascota FOREIGN KEY (mascota_id) REFERENCES mascota(mascota_id),
  CONSTRAINT fk_atencion_medico FOREIGN KEY (medico_id) REFERENCES medico(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS formula_detalle (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT, atencion_id BIGINT UNSIGNED NOT NULL,
  medicamento_id BIGINT UNSIGNED NOT NULL, cantidad_prescrita DECIMAL(12,3) NOT NULL,
  dosis TEXT NOT NULL, frecuencia VARCHAR(255) NULL, duracion VARCHAR(255) NULL,
  via_administracion VARCHAR(100) NULL, indicaciones TEXT NULL,
  cantidad_dispensada DECIMAL(12,3) NOT NULL DEFAULT 0,
  PRIMARY KEY(id), KEY idx_formula_atencion (atencion_id),
  CONSTRAINT fk_formula_atencion FOREIGN KEY (atencion_id) REFERENCES atencion_medica(id),
  CONSTRAINT fk_formula_medicamento FOREIGN KEY (medicamento_id) REFERENCES medicamento(id),
  CONSTRAINT chk_formula_cantidad CHECK (cantidad_prescrita > 0 AND cantidad_dispensada >= 0 AND cantidad_dispensada <= cantidad_prescrita)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS tarifa_servicio (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT, especializacion_id BIGINT UNSIGNED NULL,
  nombre VARCHAR(160) NOT NULL, descripcion VARCHAR(500) NULL, precio DECIMAL(12,2) NOT NULL,
  iva_porcentaje DECIMAL(5,2) NOT NULL DEFAULT 0, activo TINYINT(1) NOT NULL DEFAULT 1,
  vigente_desde DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, vigente_hasta DATETIME NULL,
  PRIMARY KEY(id), KEY idx_tarifa_vigencia (nombre, vigente_desde, vigente_hasta),
  CONSTRAINT fk_tarifa_especializacion FOREIGN KEY (especializacion_id) REFERENCES especializacion(id),
  CONSTRAINT chk_tarifa_valores CHECK (precio >= 0 AND iva_porcentaje BETWEEN 0 AND 100)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS factura (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT, numero VARCHAR(40) NOT NULL,
  atencion_id BIGINT UNSIGNED NOT NULL, cliente_id BIGINT UNSIGNED NOT NULL,
  mascota_id INT NOT NULL, medico_id BIGINT UNSIGNED NOT NULL,
  clinica_snapshot JSON NOT NULL, propietario_snapshot JSON NOT NULL, mascota_snapshot JSON NOT NULL, medico_snapshot JSON NOT NULL,
  subtotal DECIMAL(12,2) NOT NULL, impuestos DECIMAL(12,2) NOT NULL DEFAULT 0,
  descuentos DECIMAL(12,2) NOT NULL DEFAULT 0, total DECIMAL(12,2) NOT NULL,
  estado ENUM('PENDIENTE','PARCIAL','PAGADA','ANULADA') NOT NULL DEFAULT 'PENDIENTE',
  emitida_en DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, anulada_en DATETIME NULL,
  motivo_anulacion VARCHAR(500) NULL, PRIMARY KEY(id), UNIQUE KEY uk_factura_numero(numero),
  UNIQUE KEY uk_factura_atencion(atencion_id), KEY idx_factura_cliente (cliente_id, emitida_en),
  CONSTRAINT fk_factura_atencion FOREIGN KEY (atencion_id) REFERENCES atencion_medica(id),
  CONSTRAINT fk_factura_cliente FOREIGN KEY (cliente_id) REFERENCES cliente(cliente_id),
  CONSTRAINT fk_factura_mascota FOREIGN KEY (mascota_id) REFERENCES mascota(mascota_id),
  CONSTRAINT fk_factura_medico FOREIGN KEY (medico_id) REFERENCES medico(id),
  CONSTRAINT chk_factura_valores CHECK (subtotal >= 0 AND impuestos >= 0 AND descuentos >= 0 AND total >= 0)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS factura_detalle (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT, factura_id BIGINT UNSIGNED NOT NULL,
  tipo ENUM('SERVICIO','MEDICAMENTO') NOT NULL, descripcion VARCHAR(255) NOT NULL,
  medicamento_id BIGINT UNSIGNED NULL, lote_id BIGINT UNSIGNED NULL, despacho_detalle_id BIGINT UNSIGNED NULL,
  cantidad DECIMAL(12,3) NOT NULL, precio_unitario DECIMAL(12,2) NOT NULL,
  impuesto_porcentaje DECIMAL(5,2) NOT NULL DEFAULT 0, subtotal DECIMAL(12,2) NOT NULL,
  PRIMARY KEY(id), KEY idx_factura_detalle (factura_id),
  CONSTRAINT fk_detalle_factura FOREIGN KEY (factura_id) REFERENCES factura(id),
  CONSTRAINT fk_detalle_medicamento FOREIGN KEY (medicamento_id) REFERENCES medicamento(id),
  CONSTRAINT fk_detalle_lote FOREIGN KEY (lote_id) REFERENCES inventario_lote(id),
  CONSTRAINT uk_factura_despacho UNIQUE (despacho_detalle_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS factura_pago (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT, factura_id BIGINT UNSIGNED NOT NULL,
  monto DECIMAL(12,2) NOT NULL, medio VARCHAR(40) NOT NULL, referencia VARCHAR(120) NULL,
  recibido_por BIGINT UNSIGNED NULL, pagado_en DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY(id), KEY idx_pago_factura (factura_id, pagado_en),
  CONSTRAINT fk_pago_factura FOREIGN KEY (factura_id) REFERENCES factura(id),
  CONSTRAINT fk_pago_usuario FOREIGN KEY (recibido_por) REFERENCES usuario(id),
  CONSTRAINT chk_pago_monto CHECK (monto > 0)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS despacho (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT, numero VARCHAR(40) NOT NULL,
  idempotency_key VARCHAR(80) NOT NULL, request_hash CHAR(64) NOT NULL,
  estado ENUM('PENDIENTE','EN_PREPARACION','PARCIAL','DESPACHADO','PENDIENTE_DISPONIBILIDAD','CANCELADO') NOT NULL DEFAULT 'PENDIENTE',
  usuario_id BIGINT UNSIGNED NULL, creado_en DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  confirmado_en DATETIME NULL, PRIMARY KEY(id), UNIQUE KEY uk_despacho_numero(numero), UNIQUE KEY uk_despacho_idempotencia(idempotency_key),
  CONSTRAINT fk_despacho_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS despacho_detalle (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT, despacho_id BIGINT UNSIGNED NOT NULL,
  formula_detalle_id BIGINT UNSIGNED NOT NULL, lote_id BIGINT UNSIGNED NOT NULL,
  cantidad DECIMAL(12,3) NOT NULL, creado_en DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY(id), KEY idx_despacho_formula (formula_detalle_id),
  CONSTRAINT fk_despacho_detalle_despacho FOREIGN KEY (despacho_id) REFERENCES despacho(id),
  CONSTRAINT fk_despacho_detalle_formula FOREIGN KEY (formula_detalle_id) REFERENCES formula_detalle(id),
  CONSTRAINT fk_despacho_detalle_lote FOREIGN KEY (lote_id) REFERENCES inventario_lote(id),
  CONSTRAINT chk_despacho_cantidad CHECK (cantidad > 0)
) ENGINE=InnoDB;

ALTER TABLE factura_detalle ADD CONSTRAINT fk_detalle_despacho
  FOREIGN KEY (despacho_detalle_id) REFERENCES despacho_detalle(id);

CREATE TABLE IF NOT EXISTS despacho_historial (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT, despacho_id BIGINT UNSIGNED NOT NULL,
  estado_anterior VARCHAR(32) NULL, estado_nuevo VARCHAR(32) NOT NULL, motivo VARCHAR(500) NULL,
  usuario_id BIGINT UNSIGNED NULL, creado_en DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY(id), KEY idx_despacho_historial (despacho_id, creado_en),
  CONSTRAINT fk_despacho_historial_despacho FOREIGN KEY (despacho_id) REFERENCES despacho(id),
  CONSTRAINT fk_despacho_historial_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS despacho_reversion (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT, despacho_id BIGINT UNSIGNED NOT NULL,
  motivo VARCHAR(500) NOT NULL, autorizado_por BIGINT UNSIGNED NOT NULL,
  creada_en DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id), UNIQUE KEY uk_despacho_reversion (despacho_id),
  CONSTRAINT fk_reversion_despacho FOREIGN KEY (despacho_id) REFERENCES despacho(id),
  CONSTRAINT fk_reversion_usuario FOREIGN KEY (autorizado_por) REFERENCES usuario(id)
) ENGINE=InnoDB;

-- Perfil de despacho: reutilizar el registro si ya existe.
INSERT INTO perfil(nombre, activo, fecha_creacion)
SELECT 'DESPACHO_MEDICO', 1, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM perfil WHERE UPPER(nombre) = 'DESPACHO_MEDICO');

INSERT INTO menu(code, title, url, icon, activo, fecha_creacion)
SELECT 'despacho', 'Despacho de medicamentos', '/inicio/despacho', 'bi bi-capsule', 1, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM menu WHERE code = 'despacho');

INSERT INTO perfil_menu(perfil_id, menu_id)
SELECT p.perfil_id, m.menu_id FROM perfil p CROSS JOIN menu m
WHERE UPPER(p.nombre) = 'DESPACHO_MEDICO' AND m.code = 'despacho'
  AND NOT EXISTS (SELECT 1 FROM perfil_menu pm WHERE pm.perfil_id = p.perfil_id AND pm.menu_id = m.menu_id);

CREATE TABLE IF NOT EXISTS medico_usuario (
  medico_id BIGINT UNSIGNED NOT NULL, usuario_id BIGINT UNSIGNED NOT NULL,
  PRIMARY KEY (medico_id), UNIQUE KEY uk_medico_usuario_usuario (usuario_id),
  CONSTRAINT fk_medico_usuario_medico FOREIGN KEY (medico_id) REFERENCES medico(id),
  CONSTRAINT fk_medico_usuario_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id)
) ENGINE=InnoDB;
