-- Todos los nombres, documentos, telefonos y horarios son ficticios.
-- Ejecutar una sola vez despues de los scripts 01, 02 y 03.
-- La cuenta odontologo corresponde a Diego Rojas, con codigo 1.
USE oral_center;

START TRANSACTION;

-- Pacientes de ejemplo.
INSERT INTO paciente (id_paciente, dni, nombres, apellidos, telefono) VALUES
    (1001, '12345678', 'Ana', 'Torres Ruiz', '986500667'),
    (1002, '00467382', 'Carlos', 'Diaz Ramos', '920102801'),
    (1003, '70437727', 'Lucia', 'Sanchez Vega', '986420049'),
    (1004, '71840625', 'Mateo', 'Flores Cruz', '965246799'),
    (1005, '08635291', 'Sofia', 'Castro Leon', '970107519'),
    (1006, '45918263', 'Luis', 'Mendoza Silva', '935322658');

-- Diez pacientes ficticios adicionales.
INSERT INTO paciente (id_paciente, dni, nombres, apellidos, telefono) VALUES
    (1101, '40458380', 'Daniela', 'Vargas Salazar', '929122986'),
    (1102, '88847135', 'Andres', 'Quispe Huaman', '905416871'),
    (1103, '66515204', 'Camila', 'Ramos Paredes', '980467565'),
    (1104, '42323517', 'Sebastian', 'Rojas Castillo', '941843438'),
    (1105, '90809427', 'Valentina', 'Mendoza Torres', '950930946'),
    (1106, '94367872', 'Joaquin', 'Fernandez Ruiz', '976608165'),
    (1107, '32961639', 'Mariana', 'Gomez Aguilar', '949558538'),
    (1108, '22331245', 'Gabriel', 'Lopez Medina', '911720646'),
    (1109, '89047095', 'Isabella', 'Cruz Navarro', '975832681'),
    (1110, '79226739', 'Nicolas', 'Sanchez Rios', '959426417');

-- Una profesional adicional.
INSERT INTO odontologo (id_odontologo, dni, nombre_completo, especialidad) VALUES
    (1001, '72614853', 'Valeria Perez Soto', 'Odontologia general');

-- Los minutos sugeridos se pueden ajustar al registrar una cita.
INSERT INTO tipo_atencion (id_tipo_atencion, nombre, duracion_sugerida_minutos) VALUES
    (1001, 'Consulta general', 30),
    (1002, 'Limpieza dental', 45),
    (1003, 'Control odontologico', 20);

-- Turnos ficticios de lunes a sabado: 09:00 a 13:00 y 15:00 a 19:00.
-- El dia 1 es lunes y el dia 6 es sabado.
INSERT INTO horario (id_horario, id_odontologo, dia_semana, hora_inicio, hora_fin) VALUES
    (1001, 1, 1, '09:00', '13:00'),
    (1002, 1, 1, '15:00', '19:00'),
    (1003, 1, 2, '09:00', '13:00'),
    (1004, 1, 2, '15:00', '19:00'),
    (1005, 1, 3, '09:00', '13:00'),
    (1006, 1, 3, '15:00', '19:00'),
    (1007, 1, 4, '09:00', '13:00'),
    (1008, 1, 4, '15:00', '19:00'),
    (1009, 1, 5, '09:00', '13:00'),
    (1010, 1, 5, '15:00', '19:00'),
    (1011, 1, 6, '09:00', '13:00'),
    (1012, 1, 6, '15:00', '19:00'),
    (1013, 1001, 1, '09:00', '13:00'),
    (1014, 1001, 1, '15:00', '19:00'),
    (1015, 1001, 2, '09:00', '13:00'),
    (1016, 1001, 2, '15:00', '19:00'),
    (1017, 1001, 3, '09:00', '13:00'),
    (1018, 1001, 3, '15:00', '19:00'),
    (1019, 1001, 4, '09:00', '13:00'),
    (1020, 1001, 4, '15:00', '19:00'),
    (1021, 1001, 5, '09:00', '13:00'),
    (1022, 1001, 5, '15:00', '19:00'),
    (1023, 1001, 6, '09:00', '13:00'),
    (1024, 1001, 6, '15:00', '19:00');

-- Fechas fijas de ejemplo. Se pueden cambiar para otra demostracion.
-- Cinco citas para el 31 de octubre y tres para el 2 de noviembre.
-- Las dos citas anteriores permiten probar los filtros de estado y fecha.
INSERT INTO cita (id_cita, id_paciente, id_odontologo, id_tipo_atencion, fecha, hora_inicio, duracion_minutos, motivo, estado) VALUES
    (1001, 1001, 1, 1001, '2026-10-31', '16:00', 30, 'Consulta general de ejemplo', 'PENDIENTE'),
    (1002, 1002, 1, 1003, '2026-10-31', '17:00', 20, 'Control odontologico de ejemplo', 'CONFIRMADA'),
    (1003, 1003, 1001, 1002, '2026-10-31', '16:00', 45, 'Limpieza dental de ejemplo', 'PENDIENTE'),
    (1004, 1004, 1001, 1001, '2026-10-31', '17:00', 30, 'Consulta general de ejemplo', 'CONFIRMADA'),
    (1005, 1005, 1, 1001, '2026-10-31', '16:30', 30, 'Evaluacion de ortodoncia de ejemplo', 'PENDIENTE'),
    (1006, 1006, 1, 1001, '2026-11-02', '09:00', 30, 'Consulta general de ejemplo', 'PENDIENTE'),
    (1007, 1003, 1001, 1002, '2026-11-02', '09:30', 45, 'Limpieza dental de ejemplo', 'PENDIENTE'),
    (1008, 1002, 1, 1003, '2026-11-02', '10:00', 20, 'Control de ortodoncia de ejemplo', 'CONFIRMADA'),
    (1009, 1001, 1, 1001, '2026-10-30', '10:00', 30, 'Consulta de ejemplo ya atendida', 'ATENDIDA'),
    (1010, 1002, 1001, 1001, '2026-10-30', '11:00', 30, 'Consulta de ejemplo cancelada', 'CANCELADA');

COMMIT;
