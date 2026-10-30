-- Preparacion inicial de Oral Center. Ejecutar una vez con una base nueva.
-- NOT NULL exige un dato; CHECK revisa valores; FOREIGN KEY relaciona tablas.

CREATE DATABASE oral_center CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE oral_center;

-- 1. Datos de los pacientes.
CREATE TABLE paciente (
    id_paciente INT AUTO_INCREMENT PRIMARY KEY,
    dni VARCHAR(20) NOT NULL CHECK (TRIM(dni) <> ''),
    nombres VARCHAR(100) NOT NULL CHECK (TRIM(nombres) <> ''),
    apellidos VARCHAR(100) NOT NULL CHECK (TRIM(apellidos) <> ''),
    telefono VARCHAR(30) NOT NULL CHECK (TRIM(telefono) <> '')
);

-- 2. Datos de los odontologos.
CREATE TABLE odontologo (
    id_odontologo INT AUTO_INCREMENT PRIMARY KEY,
    dni VARCHAR(20) NOT NULL CHECK (TRIM(dni) <> ''),
    nombre_completo VARCHAR(200) NOT NULL CHECK (TRIM(nombre_completo) <> ''),
    especialidad VARCHAR(100) NOT NULL CHECK (TRIM(especialidad) <> '')
);

-- 3. Tipos de atencion y su duracion sugerida.
CREATE TABLE tipo_atencion (
    id_tipo_atencion INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL CHECK (TRIM(nombre) <> ''),
    duracion_sugerida_minutos INT NOT NULL CHECK (duracion_sugerida_minutos > 0)
);

-- 4. Los tres perfiles de acceso. Sus codigos se escriben en mayusculas.
CREATE TABLE perfil (
    id_perfil INT AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(20) COLLATE utf8mb4_bin NOT NULL UNIQUE,
    CHECK (codigo IN ('RECEPCIONISTA', 'ODONTOLOGO', 'ADMINISTRADOR'))
);

-- 5. Cuentas que permiten iniciar sesion en la aplicacion.
CREATE TABLE usuario (
    id_usuario INT AUTO_INCREMENT PRIMARY KEY,
    nombre_usuario VARCHAR(80) NOT NULL UNIQUE CHECK (TRIM(nombre_usuario) <> ''),
    clave_hash VARCHAR(512) NOT NULL CHECK (TRIM(clave_hash) <> ''),
    id_odontologo INT,
    FOREIGN KEY (id_odontologo) REFERENCES odontologo(id_odontologo)
);

-- 6. Horarios de cada odontologo. Los dias se numeran del 1 al 7.
CREATE TABLE horario (
    id_horario INT AUTO_INCREMENT PRIMARY KEY,
    id_odontologo INT NOT NULL,
    dia_semana TINYINT NOT NULL CHECK (dia_semana BETWEEN 1 AND 7),
    hora_inicio TIME NOT NULL,
    hora_fin TIME NOT NULL,
    FOREIGN KEY (id_odontologo) REFERENCES odontologo(id_odontologo),
    CHECK (hora_inicio >= '00:00:00' AND hora_fin < '24:00:00' AND hora_inicio < hora_fin),
    INDEX (id_odontologo, dia_semana, hora_inicio)
);

-- 7. Citas registradas para los pacientes.
CREATE TABLE cita (
    id_cita INT AUTO_INCREMENT PRIMARY KEY,
    id_paciente INT NOT NULL,
    id_odontologo INT NOT NULL,
    id_tipo_atencion INT NOT NULL,
    fecha DATE NOT NULL,
    hora_inicio TIME NOT NULL CHECK (hora_inicio >= '00:00:00' AND hora_inicio < '24:00:00'),
    duracion_minutos INT NOT NULL CHECK (duracion_minutos > 0),
    motivo VARCHAR(300) NOT NULL CHECK (TRIM(motivo) <> ''),
    estado VARCHAR(10) COLLATE utf8mb4_bin NOT NULL DEFAULT 'PENDIENTE',
    CHECK (estado IN ('PENDIENTE', 'CONFIRMADA', 'ATENDIDA', 'CANCELADA')),
    FOREIGN KEY (id_paciente) REFERENCES paciente(id_paciente) ON DELETE CASCADE,
    FOREIGN KEY (id_odontologo) REFERENCES odontologo(id_odontologo),
    FOREIGN KEY (id_tipo_atencion) REFERENCES tipo_atencion(id_tipo_atencion),
    INDEX (id_odontologo, fecha, hora_inicio),
    INDEX (id_paciente, fecha, hora_inicio)
);

-- 8. Una cuenta puede tener mas de un perfil.
CREATE TABLE usuario_perfil (
    id_usuario INT NOT NULL,
    id_perfil INT NOT NULL,
    PRIMARY KEY (id_usuario, id_perfil),
    FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario),
    FOREIGN KEY (id_perfil) REFERENCES perfil(id_perfil)
);
