-- =========================================================
-- Datos de prueba del segundo parcial. Se puede ejecutar varias veces:
-- primero vacia las tablas. Las fechas son RELATIVAS a hoy, asi las reglas
-- (citas futuras, citas atendidas pasadas) funcionan en cualquier momento.
-- =========================================================
USE clinica;

SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE formula_medica_detalle;
TRUNCATE TABLE formula_medica;
TRUNCATE TABLE anotaciones_historia;
TRUNCATE TABLE historias_medicas;
TRUNCATE TABLE citas;
TRUNCATE TABLE medicamento;
TRUNCATE TABLE mascota;
TRUNCATE TABLE medico;
TRUNCATE TABLE especializacion;
TRUNCATE TABLE cliente;
TRUNCATE TABLE raza;
TRUNCATE TABLE usuario;
SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO usuario (username, password_hash, rol, fecha_creacion, activo, email) VALUES
('admin', 'hash-de-ejemplo', 'ADMIN', NOW(), true, 'admin@clinica.com'),
('dra.gomez', 'hash-de-ejemplo', 'MEDICO', NOW(), true, 'laura.gomez@clinica.com'),
('juan.perez', 'hash-de-ejemplo', 'CLIENTE', NOW(), true, 'juan.perez@mail.com');

INSERT INTO cliente (tipo_documento, numero_documento, nombres, apellidos, fecha_nacimiento, genero, telefono, direccion, activo) VALUES
('CC', '123456789', 'Juan', 'Perez', '1990-05-10', 'M', '3001234567', 'Calle 1 # 2-3', true),
('CC', '111222333', 'Maria', 'Lopez', '1985-03-22', 'F', '3019876543', 'Carrera 10 # 20-30', true),
('CC', '555666777', 'Andres', 'Gonzalez', '1995-11-02', 'M', '3025557788', 'Avenida Siempre Viva 742', true);

INSERT INTO raza (nombre, especie, fecha_creacion, fecha_modificacion) VALUES
('Labrador', 'Perro', NOW(), NOW()),
('Persa', 'Gato', NOW(), NOW()),
('Criollo', 'Perro', NOW(), NOW());

-- Mascotas: 1 Firulais y 2 Luna (Juan), 3 Michi (Maria), 4 Rocky (Andres)
INSERT INTO mascota (nombre_mascota, edad, fecha_registro, fecha_modificacion, raza_id, cliente_id) VALUES
('Firulais', 3, NOW(), NOW(), 1, 1),
('Luna', 2, NOW(), NOW(), 3, 1),
('Michi', 2, NOW(), NOW(), 2, 2),
('Rocky', 5, NOW(), NOW(), 3, 3);

INSERT INTO especializacion (nombre, descripcion, codigo_especializacion) VALUES
('Medicina General', 'Consulta general veterinaria', 'MG01'),
('Cirugia', 'Procedimientos quirurgicos', 'CIR01'),
('Dermatologia', 'Enfermedades de la piel', 'DER01');

INSERT INTO medico (tipo_documento, numero_documento, nombres, apellidos, telefono, registro_profesional, especializacion_id) VALUES
('CC', '987654321', 'Laura', 'Gomez', '3109876543', 'RP-001', 1),
('CC', '444555666', 'Carlos', 'Ramirez', '3204445566', 'RP-002', 2),
('CC', '222333444', 'Diana', 'Castro', '3157778899', 'RP-003', 3);

INSERT INTO medicamento (nombre, presentacion, descripcion) VALUES
('Amoxicilina', 'Tabletas', 'Antibiotico de amplio espectro'),
('Meloxicam', 'Jarabe', 'Antiinflamatorio'),
('Ivermectina', 'Inyectable', 'Antiparasitario'),
('Cetirizina', 'Tabletas', 'Antialergico');

-- Citas: 1-5 atendidas (pasadas), 6 cancelada pasada, 7 cancelada futura (se puede reactivar), 8-14 futuras
INSERT INTO citas (mascota_id, medico_id, fecha, motivo, observaciones, estado) VALUES
(1, 1, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 20 DAY), '09:00:00'), 'Control general', 'Chequeo de rutina', 'ATENDIDA'),
(3, 2, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 15 DAY), '10:00:00'), 'Revision post cirugia', 'Evolucion favorable', 'ATENDIDA'),
(4, 3, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 10 DAY), '11:00:00'), 'Consulta dermatologica', 'Picazon en la piel', 'ATENDIDA'),
(2, 1, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 8 DAY), '15:00:00'), 'Vacunacion anual', NULL, 'ATENDIDA'),
(1, 1, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 5 DAY), '14:30:00'), 'Revision de peso', 'Sin historia clinica aun', 'ATENDIDA'),
(2, 2, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 3 DAY), '09:00:00'), 'Control', 'Cancelada por el cliente', 'CANCELADA'),
(3, 2, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 4 DAY), '10:00:00'), 'Control', 'Cancelada, se puede reactivar', 'CANCELADA'),
(1, 1, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '09:00:00'), 'Vacuna de refuerzo', NULL, 'PENDIENTE'),
(3, 2, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '10:00:00'), 'Retiro de puntos', NULL, 'CONFIRMADA'),
(4, 3, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 2 DAY), '11:00:00'), 'Control dermatologico', NULL, 'PROGRAMADA'),
(2, 1, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 2 DAY), '15:00:00'), 'Control general', NULL, 'PENDIENTE'),
(1, 2, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 3 DAY), '08:00:00'), 'Valoracion quirurgica', NULL, 'CONFIRMADA'),
(3, 1, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 5 DAY), '16:00:00'), 'Desparasitacion', NULL, 'PROGRAMADA'),
(4, 2, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 6 DAY), '09:30:00'), 'Limpieza dental', NULL, 'PENDIENTE');

-- Historias clinicas de las citas atendidas 1 a 4 (la cita 5 queda sin historia para probar la creacion)
INSERT INTO historias_medicas (mascota_id, cita_id, medico_id, fecha_consulta, fecha_creacion, motivo_consulta, diagnostico, observaciones_generales, estado) VALUES
(1, 1, 1, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 20 DAY), '09:00:00'), NOW(), 'Control general', 'Paciente sano, peso estable', 'Seguimiento cada 6 meses', 'ABIERTA'),
(3, 2, 2, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 15 DAY), '10:00:00'), NOW(), 'Revision post cirugia', 'Evolucion favorable', 'Se retiran puntos en 3 semanas', 'CERRADA'),
(4, 3, 3, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 10 DAY), '11:00:00'), NOW(), 'Consulta dermatologica', 'Dermatitis alergica leve', NULL, 'ABIERTA'),
(2, 4, 1, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 8 DAY), '15:00:00'), NOW(), 'Vacunacion anual', 'Vacunas al dia', NULL, 'ABIERTA');

-- Anotaciones (la historia 1 tiene varias para probar la paginacion y el orden)
INSERT INTO anotaciones_historia (historia_medica_id, medico_id, descripcion, observacion, diagnostico_evolucion, fecha) VALUES
(1, 1, 'Control de peso y apetito', 'Apetito normal', 'Estable', TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 19 DAY), '09:30:00')),
(1, 1, 'Revision de pelaje y piel', NULL, 'Sin hallazgos', TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 18 DAY), '09:30:00')),
(1, 1, 'Se aplica desparasitante', 'Dosis segun peso', 'Sin reacciones', TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 17 DAY), '09:30:00')),
(1, 1, 'Control de temperatura', NULL, 'Normal', TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 16 DAY), '09:30:00')),
(1, 1, 'Revision de oidos', 'Limpieza realizada', 'Sin infeccion', TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 15 DAY), '09:30:00')),
(1, 1, 'Control de peso', NULL, 'Peso estable', TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 14 DAY), '09:30:00')),
(1, 1, 'Revision general de cierre de seguimiento', 'Recomendar chequeo en seis meses', 'Alta del seguimiento', TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 13 DAY), '09:30:00')),
(2, 2, 'Revision de herida quirurgica', 'Cicatrizacion adecuada', 'Evolucion favorable', TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 14 DAY), '10:30:00')),
(2, 2, 'Control previo al retiro de puntos', NULL, 'Listo para retirar puntos', TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 12 DAY), '10:30:00')),
(3, 3, 'Se inicia tratamiento antialergico', 'Evitar el shampoo anterior', 'Dermatitis en mejoria', TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 9 DAY), '11:30:00')),
(4, 1, 'Aplicacion de vacuna multiple', 'Lote registrado en carnet', 'Sin reacciones', TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 7 DAY), '15:30:00'));

-- Formulas medicas (emision siempre posterior a la consulta de su historia)
INSERT INTO formula_medica (mascota_id, historia_medica_id, medico_id, medicamento_id, fecha_creacion, fecha_emision, dosis, unidad_dosis, frecuencia_horas, duracion_dias, indicaciones, estado) VALUES
(1, 1, 1, 1, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 19 DAY), '10:00:00'), TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 20 DAY), '09:30:00'), 250.00, 'mg', 12, 7, 'Administrar con comida', 'ACTIVA'),
(3, 2, 2, 2, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 14 DAY), '11:00:00'), TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 15 DAY), '10:30:00'), 5.00, 'ml', 24, 5, 'Dar en la manana', 'ACTIVA'),
(4, 3, 3, 4, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 9 DAY), '12:00:00'), TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 10 DAY), '11:30:00'), 10.00, 'mg', 24, 10, 'Suspender si hay somnolencia', 'ACTIVA'),
(2, 4, 1, 3, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 7 DAY), '16:00:00'), TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 8 DAY), '15:30:00'), 0.20, 'ml', 168, 2, 'Aplicacion unica por veterinario', 'ACTIVA'),
(1, 1, 1, 2, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 18 DAY), '10:00:00'), TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 19 DAY), '09:45:00'), 3.00, 'ml', 12, 3, 'Si hay molestia al caminar', 'INACTIVA'),
(1, 1, 1, 4, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 12 DAY), '10:00:00'), TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 13 DAY), '09:45:00'), 5.00, 'mg', 24, 7, 'Para la alergia estacional', 'ACTIVA'),
(3, 2, 2, 1, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 11 DAY), '10:00:00'), TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 12 DAY), '10:45:00'), 125.00, 'mg', 12, 5, 'Completar el tratamiento', 'ACTIVA'),
(4, 3, 3, 2, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 6 DAY), '10:00:00'), TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 7 DAY), '11:45:00'), 2.50, 'ml', 24, 4, 'Con alimento', 'ACTIVA');
