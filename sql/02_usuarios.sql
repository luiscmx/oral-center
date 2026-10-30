-- Datos ficticios para la primera instalacion, con las tablas vacias.
-- Para iniciar sesion, las tres cuentas usan la clave Demo2026!
USE oral_center;

INSERT INTO perfil (id_perfil, codigo) VALUES
    (1, 'RECEPCIONISTA'),
    (2, 'ODONTOLOGO'),
    (3, 'ADMINISTRADOR');

INSERT INTO odontologo (id_odontologo, dni, nombre_completo, especialidad) VALUES
    (1, '05829614', 'Diego Rojas Medina', 'Ortodoncia');

-- Las cadenas largas son las contrasenas protegidas, compatibles con Java.
INSERT INTO usuario (id_usuario, nombre_usuario, clave_hash, id_odontologo) VALUES
    (1, 'recepcion', 'pbkdf2-sha256$600000$RwDL7FrAZ9VwCj5Cdteuuw==$bTbO1D3YcrCTrDP03d2lSzP+RuGSgxvQYvhYD5C/2Uw=', NULL),
    (2, 'odontologo', 'pbkdf2-sha256$600000$zsMHq3eYtMnsUvnhrvEeaw==$JVPlD6LuTnRqu8nAmPbhwbir6vaPAcW/XDf8BTwsYZc=', 1),
    (3, 'admin', 'pbkdf2-sha256$600000$CcqXMD+IBzb8YGipXkdp6Q==$PgVCMHh8K/dbMQmQkdKte+OpdtDTR1scEthcqF0C4eg=', NULL);

-- Relacionar cada cuenta con su perfil.
INSERT INTO usuario_perfil (id_usuario, id_perfil) VALUES
    (1, 1),
    (2, 2),
    (3, 3);
