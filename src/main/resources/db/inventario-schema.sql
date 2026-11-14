-- Script incremental para el inventario básico de medicamentos/productos.
-- Aplicar una sola vez sobre la base de datos clinica antes de desplegar la versión.

ALTER TABLE medicamento
    MODIFY COLUMN fecha_compra DATE NULL,
    MODIFY COLUMN fecha_vence DATE NULL,
    ADD COLUMN codigo VARCHAR(8) NULL,
    ADD COLUMN tipo_producto VARCHAR(20) NULL,
    ADD COLUMN registro_invima VARCHAR(100) NULL,
    ADD COLUMN precio_compra DECIMAL(12, 2) NULL,
    ADD COLUMN precio_venta DECIMAL(12, 2) NULL,
    ADD COLUMN cantidad_actual INT NOT NULL DEFAULT 0,
    ADD COLUMN stock_minimo INT NULL,
    ADD COLUMN fecha_proximo_pedido DATE NULL,
    ADD UNIQUE KEY uk_medicamento_codigo (codigo),
    ADD CONSTRAINT chk_medicamento_cantidad_actual CHECK (cantidad_actual >= 0),
    ADD CONSTRAINT chk_medicamento_stock_minimo CHECK (stock_minimo IS NULL OR stock_minimo >= 0),
    ADD CONSTRAINT chk_medicamento_precios CHECK (
        (precio_compra IS NULL OR precio_compra >= 0)
        AND (precio_venta IS NULL OR precio_venta >= 0)
    );

-- Los registros antiguos quedan en PENDIENTE y con cantidad 1 para no perder las fórmulas existentes.
-- Debe revisarse/corregirse la cantidad prescrita de esos registros con información clínica real.
ALTER TABLE formula_medica
    ADD COLUMN cantidad_prescrita INT NOT NULL DEFAULT 1,
    ADD COLUMN cantidad_dispensada INT NOT NULL DEFAULT 0,
    ADD COLUMN estado_dispensacion VARCHAR(30) NOT NULL DEFAULT 'PENDIENTE',
    ADD CONSTRAINT chk_formula_cantidad_prescrita CHECK (cantidad_prescrita > 0),
    ADD CONSTRAINT chk_formula_cantidad_dispensada CHECK (
        cantidad_dispensada >= 0 AND cantidad_dispensada <= cantidad_prescrita
    );
