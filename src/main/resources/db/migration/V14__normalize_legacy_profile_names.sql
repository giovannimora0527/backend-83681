-- Consolidate legacy profile labels with the roles understood by the frontend and API.
-- Move accounts first; keep old profile rows but hide them from future assignments.
UPDATE usuario u
JOIN perfil legacy ON legacy.perfil_id = u.rol
JOIN perfil canonical ON UPPER(canonical.nombre) = 'VETERINARIO'
SET u.rol = canonical.perfil_id
WHERE UPPER(legacy.nombre) IN ('MEDICO', 'MÉDICO');

UPDATE usuario u
JOIN perfil legacy ON legacy.perfil_id = u.rol
JOIN perfil canonical ON UPPER(canonical.nombre) = 'RECEPCIONISTA'
SET u.rol = canonical.perfil_id
WHERE UPPER(legacy.nombre) = 'RECEPCION';

UPDATE perfil
SET activo = b'0', fecha_modificacion = CURRENT_TIMESTAMP(6)
WHERE UPPER(nombre) IN ('MEDICO', 'MÉDICO', 'RECEPCION');
