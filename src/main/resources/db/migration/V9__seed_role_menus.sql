INSERT INTO menu (code, title, url, icon, activo, fecha_creacion)
SELECT 'cita', 'Gestión de Citas', '/inicio/citas', 'bi bi-calendar', b'1', CURRENT_TIMESTAMP(6)
WHERE NOT EXISTS (SELECT 1 FROM menu WHERE code = 'cita');

INSERT INTO menu (code, title, url, icon, activo, fecha_creacion)
SELECT 'clientes', 'Gestión de Clientes', '/inicio/clientes', 'bi bi-person-check', b'1', CURRENT_TIMESTAMP(6)
WHERE NOT EXISTS (SELECT 1 FROM menu WHERE code = 'clientes');

INSERT INTO menu (code, title, url, icon, activo, fecha_creacion)
SELECT 'especializaciones', 'Gestión de Especializaciones', '/inicio/especializaciones', 'fa-solid fa-user-graduate', b'1', CURRENT_TIMESTAMP(6)
WHERE NOT EXISTS (SELECT 1 FROM menu WHERE code = 'especializaciones');

INSERT INTO menu (code, title, url, icon, activo, fecha_creacion)
SELECT 'formulas', 'Gestión de Fórmulas', '/inicio/formulas', 'fa-solid fa-clipboard-list', b'1', CURRENT_TIMESTAMP(6)
WHERE NOT EXISTS (SELECT 1 FROM menu WHERE code = 'formulas');

INSERT INTO menu (code, title, url, icon, activo, fecha_creacion)
SELECT 'historias', 'Gestión de Historias', '/inicio/historias', 'fa-solid fa-book-medical', b'1', CURRENT_TIMESTAMP(6)
WHERE NOT EXISTS (SELECT 1 FROM menu WHERE code = 'historias');

INSERT INTO menu (code, title, url, icon, activo, fecha_creacion)
SELECT 'mascotas', 'Gestión de Mascotas', '/inicio/mascotas', 'fa-solid fa-paw', b'1', CURRENT_TIMESTAMP(6)
WHERE NOT EXISTS (SELECT 1 FROM menu WHERE code = 'mascotas');

INSERT INTO menu (code, title, url, icon, activo, fecha_creacion)
SELECT 'medicamentos', 'Gestión de Medicamentos', '/inicio/medicamentos', 'fa-solid fa-capsules', b'1', CURRENT_TIMESTAMP(6)
WHERE NOT EXISTS (SELECT 1 FROM menu WHERE code = 'medicamentos');

INSERT INTO menu (code, title, url, icon, activo, fecha_creacion)
SELECT 'medicos', 'Gestión de Médicos', '/inicio/medicos', 'fa-solid fa-user-doctor', b'1', CURRENT_TIMESTAMP(6)
WHERE NOT EXISTS (SELECT 1 FROM menu WHERE code = 'medicos');

INSERT INTO menu (code, title, url, icon, activo, fecha_creacion)
SELECT 'usuarios', 'Gestión de Usuarios', '/inicio/usuarios', 'feather icon-users', b'1', CURRENT_TIMESTAMP(6)
WHERE NOT EXISTS (SELECT 1 FROM menu WHERE code = 'usuarios');

INSERT IGNORE INTO perfil_menu (perfil_id, menu_id)
SELECT p.perfil_id, m.menu_id
FROM perfil p
JOIN menu m ON m.code IN (
    'cita', 'clientes', 'especializaciones', 'formulas', 'historias',
    'mascotas', 'medicamentos', 'medicos', 'usuarios'
)
WHERE p.nombre = 'ADMINISTRADOR';

INSERT IGNORE INTO perfil_menu (perfil_id, menu_id)
SELECT p.perfil_id, m.menu_id
FROM perfil p
JOIN menu m ON m.code IN (
    'clientes', 'especializaciones', 'formulas', 'historias', 'mascotas',
    'medicamentos', 'medicos'
)
WHERE p.nombre = 'VETERINARIO';

INSERT IGNORE INTO perfil_menu (perfil_id, menu_id)
SELECT p.perfil_id, m.menu_id
FROM perfil p
JOIN menu m ON m.code IN (
    'cita', 'clientes', 'especializaciones', 'mascotas', 'medicos'
)
WHERE p.nombre = 'RECEPCIONISTA';
