-- Legacy imported historia_medica rows used a required cliente_id column.
-- New API rows use paciente_id; keep old values while allowing new pet records.
SET @migration_sql = IF(
    EXISTS (SELECT 1 FROM information_schema.columns
            WHERE table_schema = DATABASE() AND table_name = 'historia_medica' AND column_name = 'cliente_id'),
    'ALTER TABLE historia_medica MODIFY COLUMN cliente_id INT NULL',
    'SELECT 1'
);
PREPARE migration_stmt FROM @migration_sql;
EXECUTE migration_stmt;
DEALLOCATE PREPARE migration_stmt;
