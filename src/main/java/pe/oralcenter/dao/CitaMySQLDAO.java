package pe.oralcenter.dao;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pe.oralcenter.model.*;

public class CitaMySQLDAO implements CitaDAO {
    private static final Logger REGISTRO = LoggerFactory.getLogger(CitaMySQLDAO.class);
    private final Conexion conexion;
    private final Disponibilidad disponibilidad = new Disponibilidad();

    public CitaMySQLDAO(Conexion conexion) { this.conexion = conexion; }

    @Override public List<CitaAgenda> consultarAgenda(LocalDate fecha, Long odontologo, String estado) {
        String sql = "SELECT c.*, CONCAT(p.nombres, ' ', p.apellidos, ' · ', p.dni) AS paciente, "
                + "o.nombre_completo AS odontologo, t.nombre AS atencion FROM cita c "
                + "JOIN paciente p ON p.id_paciente = c.id_paciente "
                + "JOIN odontologo o ON o.id_odontologo = c.id_odontologo "
                + "JOIN tipo_atencion t ON t.id_tipo_atencion = c.id_tipo_atencion WHERE c.fecha = ?";
        if (odontologo != null) { sql += " AND c.id_odontologo = ?"; }
        if (!estado.isEmpty()) { sql += " AND c.estado = ?"; }
        sql += " ORDER BY c.hora_inicio, o.nombre_completo, c.id_cita";
        List<CitaAgenda> resultado = new ArrayList<>();
        try (Connection cn = conexion.abrir(); PreparedStatement consulta = cn.prepareStatement(sql)) {
            consulta.setDate(1, Date.valueOf(fecha));
            int parametro = 2;
            if (odontologo != null) {
                consulta.setLong(parametro, odontologo);
                parametro++;
            }
            if (!estado.isEmpty()) { consulta.setString(parametro, estado); }
            try (ResultSet filas = consulta.executeQuery()) {
                while (filas.next()) {
                    Cita cita = new Cita(filas.getLong("id_cita"), filas.getLong("id_paciente"), filas.getLong("id_odontologo"),
                            filas.getLong("id_tipo_atencion"), fecha, filas.getTime("hora_inicio").toLocalTime(),
                            filas.getInt("duracion_minutos"), filas.getString("motivo"), filas.getString("estado"));
                    resultado.add(new CitaAgenda(cita, filas.getString("paciente"), filas.getString("odontologo"), filas.getString("atencion")));
                }
            }
            return resultado;
        } catch (SQLException ex) {
            REGISTRO.error("Falló la consulta de agenda (código SQL {}).", ex.getErrorCode());
            throw new IllegalStateException("No se pudo consultar la agenda. Revisa la conexión con MySQL.");
        }
    }

    @Override public List<Paciente> listarPacientes(Long permitido) {
        return new PacienteMySQLDAO(conexion).listarParaCitas(permitido);
    }

    @Override public List<Odontologo> listarOdontologos(Long permitido) {
        return new OdontologoMySQLDAO(conexion).listar(permitido);
    }

    @Override public List<TipoAtencion> listarTiposAtencion() {
        List<TipoAtencion> resultado = new ArrayList<>();
        try (Connection cn = conexion.abrir(); PreparedStatement consulta = cn.prepareStatement(
                "SELECT id_tipo_atencion, nombre, duracion_sugerida_minutos FROM tipo_atencion ORDER BY nombre, id_tipo_atencion");
                ResultSet filas = consulta.executeQuery()) {
            while (filas.next()) {
                resultado.add(new TipoAtencion(filas.getLong("id_tipo_atencion"), filas.getString("nombre"), filas.getInt("duracion_sugerida_minutos")));
            }
            return resultado;
        } catch (SQLException ex) {
            REGISTRO.error("Falló la consulta de atenciones (código SQL {}).", ex.getErrorCode());
            throw new IllegalStateException("No se pudieron consultar los tipos de atención. Revisa la conexión con MySQL.");
        }
    }

    @Override public List<Intervalo> consultarDisponibilidad(long odontologo, LocalDate fecha, int duracion) {
        try (Connection cn = conexion.abrir()) {
            // Las dos listas se leen en la misma transacción para consultar una sola versión.
            cn.setAutoCommit(false);
            try {
                List<Intervalo> libres = leerDisponibilidad(cn, odontologo, fecha, duracion);
                cn.commit();
                return libres;
            } catch (SQLException | RuntimeException ex) { cn.rollback(); throw ex; }
        } catch (SQLException ex) {
            REGISTRO.error("Falló la consulta de disponibilidad (código SQL {}).", ex.getErrorCode());
            throw new IllegalStateException("No se pudo consultar la disponibilidad. Revisa la conexión con MySQL.");
        }
    }

    private List<Intervalo> leerDisponibilidad(Connection cn, long odontologo, LocalDate fecha, int duracion) throws SQLException {
        List<Horario> horarios = new ArrayList<>();
        try (PreparedStatement consulta = cn.prepareStatement("SELECT id_horario, dia_semana, hora_inicio, hora_fin FROM horario WHERE id_odontologo = ? AND dia_semana = ?")) {
            consulta.setLong(1, odontologo);
            consulta.setInt(2, fecha.getDayOfWeek().getValue());
            try (ResultSet filas = consulta.executeQuery()) {
                while (filas.next()) {
                    horarios.add(new Horario(filas.getLong("id_horario"), odontologo, filas.getInt("dia_semana"),
                            filas.getTime("hora_inicio").toLocalTime(), filas.getTime("hora_fin").toLocalTime()));
                }
            }
        }
        List<Cita> citas = new ArrayList<>();
        String sql = "SELECT id_cita, id_paciente, id_tipo_atencion, hora_inicio, duracion_minutos, motivo, estado FROM cita "
                + "WHERE id_odontologo = ? AND fecha = ? AND estado IN ('PENDIENTE', 'CONFIRMADA')";
        try (PreparedStatement consulta = cn.prepareStatement(sql)) {
            consulta.setLong(1, odontologo);
            consulta.setDate(2, Date.valueOf(fecha));
            try (ResultSet filas = consulta.executeQuery()) {
                while (filas.next()) {
                    citas.add(new Cita(filas.getLong("id_cita"), filas.getLong("id_paciente"), odontologo, filas.getLong("id_tipo_atencion"), fecha,
                            filas.getTime("hora_inicio").toLocalTime(), filas.getInt("duracion_minutos"), filas.getString("motivo"), filas.getString("estado")));
                }
            }
        }
        LocalDateTime ahora = leerAhora(cn);
        if (fecha.isBefore(ahora.toLocalDate())) {
            throw new IllegalArgumentException("La fecha indicada ya pasó. Elige otra fecha.");
        }
        return disponibilidad.calcular(odontologo, fecha, duracion, horarios, citas, ahora);
    }

    @Override public long guardar(Cita cita, Long permitido) {
        if (permitido != null && permitido.longValue() != cita.getIdOdontologo()) {
            throw new IllegalStateException("Solo puedes gestionar citas de tu propio odontólogo.");
        }
        try (Connection cn = conexion.abrir()) {
            cn.setAutoCommit(false);
            try {
                // Es la misma fila que bloquea el guardado de horarios del odontólogo.
                try (PreparedStatement consulta = cn.prepareStatement("SELECT id_odontologo FROM odontologo WHERE id_odontologo = ? FOR UPDATE")) {
                    consulta.setLong(1, cita.getIdOdontologo());
                    try (ResultSet fila = consulta.executeQuery()) {
                        if (!fila.next()) {
                            throw new IllegalArgumentException("El odontólogo ya no existe. Actualiza los datos.");
                        }
                    }
                }
                comprobarReferencias(cn, cita, permitido);
                if (!cita.getInicio().toLocalDate().equals(cita.getFin().toLocalDate())) {
                    throw new IllegalArgumentException("La cita completa debe quedar dentro del mismo día.");
                }
                boolean disponible = false;
                for (Intervalo libre : leerDisponibilidad(cn, cita.getIdOdontologo(), cita.getFecha(), cita.getDuracion())) {
                    if (libre.contiene(cita.getInicio(), cita.getFin())) {
                        disponible = true;
                        break;
                    }
                }
                if (cita.getInicio().isBefore(leerAhora(cn))) {
                    throw new IllegalArgumentException("La fecha y hora ya pasaron. Elige un inicio futuro.");
                }
                if (!disponible) {
                    throw new IllegalArgumentException("El intervalo está ocupado o queda fuera del turno. Consulta nuevamente la disponibilidad.");
                }
                long id = insertar(cn, cita);
                cn.commit();
                return id;
            } catch (SQLException | RuntimeException ex) { cn.rollback(); throw ex; }
        } catch (SQLException ex) {
            REGISTRO.error("Falló el guardado de cita (código SQL {}).", ex.getErrorCode());
            throw new IllegalStateException("No se pudo guardar la cita. Revisa la conexión con MySQL y consulta antes de reintentar.");
        }
    }

    private void comprobarReferencias(Connection cn, Cita cita, Long permitido) throws SQLException {
        try (PreparedStatement consulta = cn.prepareStatement("SELECT id_paciente FROM paciente WHERE id_paciente = ? FOR SHARE")) {
            consulta.setLong(1, cita.getIdPaciente());
            try (ResultSet fila = consulta.executeQuery()) {
                if (!fila.next()) {
                    throw new IllegalArgumentException("El paciente ya no existe. Actualiza los datos.");
                }
            }
        }
        try (PreparedStatement consulta = cn.prepareStatement("SELECT id_tipo_atencion FROM tipo_atencion WHERE id_tipo_atencion = ? FOR SHARE")) {
            consulta.setLong(1, cita.getIdTipoAtencion());
            try (ResultSet fila = consulta.executeQuery()) {
                if (!fila.next()) {
                    throw new IllegalArgumentException("El tipo de atención ya no existe. Actualiza los datos.");
                }
            }
        }
        if (permitido != null) {
            try (PreparedStatement consulta = cn.prepareStatement("SELECT id_cita FROM cita WHERE id_paciente = ? AND id_odontologo = ? LIMIT 1")) {
                consulta.setLong(1, cita.getIdPaciente());
                consulta.setLong(2, permitido);
                try (ResultSet fila = consulta.executeQuery()) {
                    if (!fila.next()) {
                        throw new IllegalStateException("Solo puedes registrar citas de tus pacientes asociados. Recepción puede registrar la primera cita.");
                    }
                }
            }
        }
    }

    private LocalDateTime leerAhora(Connection cn) throws SQLException {
        try (PreparedStatement consulta = cn.prepareStatement("SELECT CURRENT_TIMESTAMP(6)"); ResultSet fila = consulta.executeQuery()) {
            fila.next();
            return fila.getObject(1, LocalDateTime.class);
        }
    }

    private long insertar(Connection cn, Cita cita) throws SQLException {
        String sql = "INSERT INTO cita (id_paciente, id_odontologo, id_tipo_atencion, fecha, hora_inicio, duracion_minutos, motivo, estado) VALUES (?, ?, ?, ?, ?, ?, ?, 'PENDIENTE')";
        try (PreparedStatement consulta = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            consulta.setLong(1, cita.getIdPaciente());
            consulta.setLong(2, cita.getIdOdontologo());
            consulta.setLong(3, cita.getIdTipoAtencion());
            consulta.setDate(4, Date.valueOf(cita.getFecha()));
            consulta.setTime(5, Time.valueOf(cita.getHora()));
            consulta.setInt(6, cita.getDuracion());
            consulta.setString(7, cita.getMotivo());
            if (consulta.executeUpdate() != 1) {
                throw new IllegalStateException("No se confirmó el guardado de la cita.");
            }
            try (ResultSet clave = consulta.getGeneratedKeys()) {
                if (!clave.next()) {
                    throw new IllegalStateException("No se obtuvo el código de la cita.");
                }
                return clave.getLong(1);
            }
        }
    }
}
