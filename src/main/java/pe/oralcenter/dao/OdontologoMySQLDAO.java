package pe.oralcenter.dao;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pe.oralcenter.model.Horario;
import pe.oralcenter.model.Odontologo;

/** Guarda odontólogos y sus turnos semanales mediante consultas con parámetros. */
public class OdontologoMySQLDAO implements OdontologoDAO {
    private static final Logger REGISTRO = LoggerFactory.getLogger(OdontologoMySQLDAO.class);
    private final Conexion conexion;

    public OdontologoMySQLDAO(Conexion conexion) { this.conexion = conexion; }

    @Override public void guardar(Odontologo odontologo) {
        String sql = "INSERT INTO odontologo (dni, nombre_completo, especialidad) VALUES (?, ?, ?)";
        try (Connection cn = conexion.abrir(); PreparedStatement consulta = cn.prepareStatement(sql)) {
            consulta.setString(1, odontologo.getDni());
            consulta.setString(2, odontologo.getNombreCompleto());
            consulta.setString(3, odontologo.getEspecialidad());
            consulta.executeUpdate();
        } catch (SQLException ex) {
            REGISTRO.error("Falló el registro de odontólogo (código SQL {}).", ex.getErrorCode());
            throw new IllegalStateException("No se pudo guardar el odontólogo. Comprueba la conexión con MySQL.");
        }
    }

    @Override public void actualizar(Odontologo odontologo) {
        String sql = "UPDATE odontologo SET dni = ?, nombre_completo = ?, especialidad = ? WHERE id_odontologo = ?";
        try (Connection cn = conexion.abrir(); PreparedStatement consulta = cn.prepareStatement(sql)) {
            consulta.setString(1, odontologo.getDni());
            consulta.setString(2, odontologo.getNombreCompleto());
            consulta.setString(3, odontologo.getEspecialidad());
            consulta.setLong(4, odontologo.getId());
            if (consulta.executeUpdate() != 1) {
                throw new IllegalStateException("El odontólogo ya no existe. Actualiza el listado.");
            }
        } catch (SQLException ex) {
            REGISTRO.error("Falló la actualización de odontólogo (código SQL {}).", ex.getErrorCode());
            throw new IllegalStateException("No se pudo actualizar el odontólogo. Comprueba la conexión con MySQL.");
        }
    }

    @Override public List<Odontologo> listar() {
        return listar(null);
    }

    public List<Odontologo> listar(Long id) {
        String sql = "SELECT id_odontologo, dni, nombre_completo, especialidad FROM odontologo";
        if (id != null) { sql += " WHERE id_odontologo = ?"; }
        sql += " ORDER BY nombre_completo, id_odontologo";
        List<Odontologo> resultado = new ArrayList<>();
        try (Connection cn = conexion.abrir(); PreparedStatement consulta = cn.prepareStatement(sql)) {
            if (id != null) { consulta.setLong(1, id); }
            try (ResultSet filas = consulta.executeQuery()) {
                while (filas.next()) {
                    resultado.add(new Odontologo(filas.getLong("id_odontologo"), filas.getString("dni"),
                            filas.getString("nombre_completo"), filas.getString("especialidad")));
                }
            }
            return resultado;
        } catch (SQLException ex) {
            REGISTRO.error("Falló la consulta de odontólogos (código SQL {}).", ex.getErrorCode());
            throw new IllegalStateException("No se pudo consultar odontólogos. Comprueba la conexión con MySQL.");
        }
    }

    @Override public List<Horario> listarHorarios(long odontologo) {
        try (Connection cn = conexion.abrir()) {
            return consultarHorarios(cn, odontologo);
        } catch (SQLException ex) {
            REGISTRO.error("Falló la consulta de horarios (código SQL {}).", ex.getErrorCode());
            throw new IllegalStateException("No se pudo consultar horarios. Comprueba la conexión con MySQL.");
        }
    }

    private List<Horario> consultarHorarios(Connection cn, long odontologo) throws SQLException {
        String sql = "SELECT id_horario, dia_semana, hora_inicio, hora_fin FROM horario WHERE id_odontologo = ? ORDER BY dia_semana, hora_inicio, id_horario";
        List<Horario> resultado = new ArrayList<>();
        try (PreparedStatement consulta = cn.prepareStatement(sql)) {
            consulta.setLong(1, odontologo);
            try (ResultSet filas = consulta.executeQuery()) {
                while (filas.next()) {
                    resultado.add(new Horario(filas.getLong("id_horario"), odontologo, filas.getInt("dia_semana"),
                            filas.getTime("hora_inicio").toLocalTime(), filas.getTime("hora_fin").toLocalTime()));
                }
            }
        }
        return resultado;
    }

    @Override public void guardarHorario(Horario horario) {
        try (Connection cn = conexion.abrir()) {
            cn.setAutoCommit(false);
            try {
                // Un solo guardado a la vez por odontólogo: evita cruces entre dos ventanas.
                bloquearOdontologo(cn, horario.getIdOdontologo());
                List<Horario> actuales = consultarHorarios(cn, horario.getIdOdontologo());
                Horario anterior = null;
                List<Horario> propuestos = new ArrayList<>();
                for (Horario otro : actuales) {
                    if (otro.getId() == horario.getId()) {
                        anterior = otro;
                    } else {
                        if (otro.seCruzaCon(horario)) {
                            throw new IllegalArgumentException("El horario se cruza con otro turno del odontólogo.");
                        }
                        propuestos.add(otro);
                    }
                }
                if (horario.getId() > 0 && anterior == null) {
                    throw new IllegalStateException("El horario ya no existe para este odontólogo. Actualiza el listado.");
                }
                propuestos.add(horario);
                if (anterior != null) {
                    comprobarCitas(cn, anterior, propuestos);
                }
                escribirHorario(cn, horario);
                cn.commit();
            } catch (SQLException | RuntimeException ex) {
                cn.rollback();
                throw ex;
            }
        } catch (SQLException ex) {
            REGISTRO.error("Falló el guardado de horario (código SQL {}).", ex.getErrorCode());
            throw new IllegalStateException("No se pudo guardar el horario. Comprueba la conexión con MySQL.");
        }
    }

    private void bloquearOdontologo(Connection cn, long odontologo) throws SQLException {
        try (PreparedStatement consulta = cn.prepareStatement("SELECT id_odontologo FROM odontologo WHERE id_odontologo = ? FOR UPDATE")) {
            consulta.setLong(1, odontologo);
            try (ResultSet fila = consulta.executeQuery()) {
                if (!fila.next()) {
                    throw new IllegalStateException("El odontólogo ya no existe. Actualiza el listado.");
                }
            }
        }
    }

    private void comprobarCitas(Connection cn, Horario anterior, List<Horario> propuestos) throws SQLException {
        String sql = "SELECT fecha, hora_inicio, duracion_minutos FROM cita WHERE id_odontologo = ? "
                + "AND fecha >= CURRENT_DATE AND estado IN ('PENDIENTE', 'CONFIRMADA')";
        try (PreparedStatement consulta = cn.prepareStatement(sql)) {
            consulta.setLong(1, anterior.getIdOdontologo());
            try (ResultSet filas = consulta.executeQuery()) {
                while (filas.next()) {
                    LocalDateTime inicio = LocalDateTime.of(filas.getDate("fecha").toLocalDate(), filas.getTime("hora_inicio").toLocalTime());
                    LocalDateTime fin = inicio.plusMinutes(filas.getInt("duracion_minutos"));
                    if (anterior.cubre(inicio, fin)) {
                        boolean cubierta = false;
                        for (Horario turno : propuestos) {
                            if (turno.cubre(inicio, fin)) {
                                cubierta = true;
                                break;
                            }
                        }
                        if (!cubierta) {
                            throw new IllegalArgumentException("El cambio dejaría una cita pendiente o confirmada fuera de horario. Reprograma esa cita primero.");
                        }
                    }
                }
            }
        }
    }

    private void escribirHorario(Connection cn, Horario horario) throws SQLException {
        String sql = "INSERT INTO horario (dia_semana, hora_inicio, hora_fin, id_odontologo) VALUES (?, ?, ?, ?)";
        if (horario.getId() > 0) {
            sql = "UPDATE horario SET dia_semana = ?, hora_inicio = ?, hora_fin = ? WHERE id_odontologo = ? AND id_horario = ?";
        }
        try (PreparedStatement consulta = cn.prepareStatement(sql)) {
            consulta.setInt(1, horario.getDiaSemana());
            consulta.setTime(2, Time.valueOf(horario.getInicio()));
            consulta.setTime(3, Time.valueOf(horario.getFin()));
            consulta.setLong(4, horario.getIdOdontologo());
            if (horario.getId() > 0) {
                consulta.setLong(5, horario.getId());
            }
            if (consulta.executeUpdate() != 1) {
                throw new IllegalStateException("El horario ya no existe. Actualiza el listado.");
            }
        }
    }
}
