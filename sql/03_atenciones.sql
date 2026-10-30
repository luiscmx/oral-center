-- Opcion ficticia para probar citas. Ejecutar una vez si el catalogo esta vacio.
-- Los 30 minutos son una sugerencia de demostracion, ajustable en cada cita.
USE oral_center;

INSERT INTO tipo_atencion (nombre, duracion_sugerida_minutos) VALUES
    ('Atención de demostración', 30);
