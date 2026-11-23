-- Align legacy/imported tables with the current JPA model without dropping data.

SET @migration_sql = IF(
    EXISTS (SELECT 1 FROM information_schema.columns
            WHERE table_schema = DATABASE() AND table_name = 'mascota' AND column_name = 'raza_id'),
    'SELECT 1',
    IF(
        EXISTS (SELECT 1 FROM information_schema.columns
                WHERE table_schema = DATABASE() AND table_name = 'mascota' AND column_name = 'traza_id'),
        'ALTER TABLE mascota CHANGE COLUMN traza_id raza_id INT NOT NULL',
        'ALTER TABLE mascota ADD COLUMN raza_id INT NULL'
    )
);
PREPARE migration_stmt FROM @migration_sql;
EXECUTE migration_stmt;
DEALLOCATE PREPARE migration_stmt;

SET @migration_sql = IF(
    EXISTS (SELECT 1 FROM information_schema.columns
            WHERE table_schema = DATABASE() AND table_name = 'mascota' AND column_name = 'activo'),
    'SELECT 1',
    'ALTER TABLE mascota ADD COLUMN activo BIT NULL DEFAULT b''1'''
);
PREPARE migration_stmt FROM @migration_sql;
EXECUTE migration_stmt;
DEALLOCATE PREPARE migration_stmt;

SET @migration_sql = IF(
    EXISTS (SELECT 1 FROM information_schema.columns
            WHERE table_schema = DATABASE() AND table_name = 'medico' AND column_name = 'estado'),
    'SELECT 1',
    'ALTER TABLE medico ADD COLUMN estado BIT NOT NULL DEFAULT b''1'''
);
PREPARE migration_stmt FROM @migration_sql;
EXECUTE migration_stmt;
DEALLOCATE PREPARE migration_stmt;

SET @migration_sql = IF(
    EXISTS (SELECT 1 FROM information_schema.columns
            WHERE table_schema = DATABASE() AND table_name = 'formula_medica' AND column_name = 'duracion'),
    'SELECT 1',
    'ALTER TABLE formula_medica ADD COLUMN duracion VARCHAR(255) NULL'
);
PREPARE migration_stmt FROM @migration_sql;
EXECUTE migration_stmt;
DEALLOCATE PREPARE migration_stmt;

-- Keep the imported cliente_id column and its values; legacy records cannot be
-- safely mapped to a pet when a client has zero or multiple pets.
SET @migration_sql = IF(
    EXISTS (SELECT 1 FROM information_schema.columns
            WHERE table_schema = DATABASE() AND table_name = 'historia_medica' AND column_name = 'paciente_id'),
    'SELECT 1',
    'ALTER TABLE historia_medica ADD COLUMN paciente_id INT NULL'
);
PREPARE migration_stmt FROM @migration_sql;
EXECUTE migration_stmt;
DEALLOCATE PREPARE migration_stmt;

SET @migration_sql = IF(
    EXISTS (SELECT 1 FROM information_schema.statistics
            WHERE table_schema = DATABASE() AND table_name = 'historia_medica' AND column_name = 'paciente_id'),
    'SELECT 1',
    'CREATE INDEX ix_historia_paciente_compat ON historia_medica (paciente_id)'
);
PREPARE migration_stmt FROM @migration_sql;
EXECUTE migration_stmt;
DEALLOCATE PREPARE migration_stmt;

SET @migration_sql = IF(
    EXISTS (SELECT 1 FROM information_schema.key_column_usage
            WHERE table_schema = DATABASE() AND table_name = 'historia_medica'
              AND column_name = 'paciente_id' AND referenced_table_name = 'mascota'),
    'SELECT 1',
    'ALTER TABLE historia_medica ADD CONSTRAINT fk_historia_mascota_compat FOREIGN KEY (paciente_id) REFERENCES mascota (mascota_id)'
);
PREPARE migration_stmt FROM @migration_sql;
EXECUTE migration_stmt;
DEALLOCATE PREPARE migration_stmt;
