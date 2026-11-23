-- Legacy data predates the current prescription and clinical note fields.
-- Nullable columns preserve historic rows; current API writes fill them.

SET @migration_sql = IF(
    EXISTS (SELECT 1 FROM information_schema.columns
            WHERE table_schema = DATABASE() AND table_name = 'formula_medica' AND column_name = 'frecuencia'),
    'SELECT 1',
    'ALTER TABLE formula_medica ADD COLUMN frecuencia VARCHAR(255) NULL'
);
PREPARE migration_stmt FROM @migration_sql;
EXECUTE migration_stmt;
DEALLOCATE PREPARE migration_stmt;

SET @migration_sql = IF(
    EXISTS (SELECT 1 FROM information_schema.columns
            WHERE table_schema = DATABASE() AND table_name = 'anotacion_historia' AND column_name = 'cita_id'),
    'SELECT 1',
    'ALTER TABLE anotacion_historia ADD COLUMN cita_id BIGINT NULL'
);
PREPARE migration_stmt FROM @migration_sql;
EXECUTE migration_stmt;
DEALLOCATE PREPARE migration_stmt;

SET @migration_sql = IF(
    EXISTS (SELECT 1 FROM information_schema.columns
            WHERE table_schema = DATABASE() AND table_name = 'anotacion_historia' AND column_name = 'diagnostico'),
    'SELECT 1',
    'ALTER TABLE anotacion_historia ADD COLUMN diagnostico TEXT NULL'
);
PREPARE migration_stmt FROM @migration_sql;
EXECUTE migration_stmt;
DEALLOCATE PREPARE migration_stmt;

SET @migration_sql = IF(
    EXISTS (SELECT 1 FROM information_schema.columns
            WHERE table_schema = DATABASE() AND table_name = 'anotacion_historia' AND column_name = 'observaciones'),
    'SELECT 1',
    'ALTER TABLE anotacion_historia ADD COLUMN observaciones TEXT NULL'
);
PREPARE migration_stmt FROM @migration_sql;
EXECUTE migration_stmt;
DEALLOCATE PREPARE migration_stmt;
