package pe.oralcenter.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pe.oralcenter.model.Paciente;

/** Guarda y consulta pacientes utilizando JDBC. */
public class PacienteMySQLDAO implements PacienteDAO {
    private static final Logger REGISTRO = LoggerFactory.getLogger(PacienteMySQLDAO.class);
    private final Conexion conexion;

    public PacienteMySQLDAO(Conexion conexion) {
        this.conexion = conexion;
    }

    @Override public void guardar(Paciente paciente) {
        String sql = "INSERT INTO paciente (dni, nombres, apellidos, telefono) VALUES (?, ?, ?, ?)";
        try (Connection cn = conexion.abrir(); PreparedStatement consulta = cn.prepareStatement(sql)) {
            consulta.setString(1, paciente.getDni());
            consulta.setString(2, paciente.getNombres());
            consulta.setString(3, paciente.getApellidos());
            consulta.setString(4, paciente.getTelefono());
            if (consulta.executeUpdate() != 1) {
                throw new IllegalStateException("No se confirmó el guardado del paciente.");
            }
        } catch (SQLException ex) {
            REGISTRO.error("Falló el registro de paciente (código SQL {}).", ex.getErrorCode());
            throw new IllegalStateException("No se pudo guardar el paciente. Revisa la conexión con MySQL.", ex);
        }
    }

    @Override public List<Paciente> listar() {
        return buscar("");
    }

    @Override public void eliminar(long id) {
        String sql = "DELETE FROM paciente WHERE id_paciente = ?";
        try (Connection cn = conexion.abrir(); PreparedStatement consulta = cn.prepareStatement(sql)) {
            consulta.setLong(1, id);
            if (consulta.executeUpdate() != 1) {
                throw new IllegalStateException("El paciente ya no existe. Busca de nuevo para actualizar la lista.");
            }
        } catch (SQLException ex) {
            REGISTRO.error("Falló la eliminación de paciente (código SQL {}).", ex.getErrorCode());
            throw new IllegalStateException("No se pudo eliminar el paciente. Revisa la conexión con MySQL.", ex);
        }
    }

    @Override public void actualizar(Paciente paciente) {
        String sql = "UPDATE paciente SET dni = ?, nombres = ?, apellidos = ?, telefono = ? "
                + "WHERE id_paciente = ?";
        try (Connection cn = conexion.abrir(); PreparedStatement consulta = cn.prepareStatement(sql)) {
            consulta.setString(1, paciente.getDni());
            consulta.setString(2, paciente.getNombres());
            consulta.setString(3, paciente.getApellidos());
            consulta.setString(4, paciente.getTelefono());
            consulta.setLong(5, paciente.getId());
            if (consulta.executeUpdate() != 1) {
                throw new IllegalStateException("El paciente ya no existe. Busca de nuevo para actualizar la lista.");
            }
        } catch (SQLException ex) {
            REGISTRO.error("Falló la actualización de paciente (código SQL {}).", ex.getErrorCode());
            throw new IllegalStateException("No se pudieron actualizar los datos del paciente. Revisa la conexión con MySQL.", ex);
        }
    }

    @Override public List<Paciente> buscar(String texto) {
        return consultar(texto, null);
    }

    @Override public List<Paciente> buscarAsociados(String texto, long odontologo) {
        return consultar(texto, odontologo);
    }

    public List<Paciente> listarParaCitas(Long odontologo) {
        return consultar("", odontologo);
    }

    private List<Paciente> consultar(String texto, Long odontologo) {
        // LOCATE devuelve una posición mayor que cero cuando encuentra el texto.
        String sql = "SELECT id_paciente, dni, nombres, apellidos, telefono FROM paciente "
                + "WHERE (LOCATE(?, dni) > 0 OR LOCATE(?, CONCAT(nombres, ' ', apellidos)) > 0)";
        if (odontologo != null) {
            sql += " AND EXISTS (SELECT 1 FROM cita WHERE cita.id_paciente = paciente.id_paciente AND id_odontologo = ?)";
        }
        sql += " ORDER BY apellidos, nombres, id_paciente";
        List<Paciente> pacientes = new ArrayList<>();
        try (Connection cn = conexion.abrir(); PreparedStatement consulta = cn.prepareStatement(sql)) {
            consulta.setString(1, texto);
            consulta.setString(2, texto);
            if (odontologo != null) {
                consulta.setLong(3, odontologo);
            }
            try (ResultSet filas = consulta.executeQuery()) {
                while (filas.next()) {
                    pacientes.add(new Paciente(filas.getLong("id_paciente"), filas.getString("dni"),
                            filas.getString("nombres"), filas.getString("apellidos"), filas.getString("telefono")));
                }
            }
            return pacientes;
        } catch (SQLException ex) {
            REGISTRO.error("Falló la consulta de pacientes (código SQL {}).", ex.getErrorCode());
            throw new IllegalStateException("No se pudo cargar la lista de pacientes. Revisa la conexión con MySQL.", ex);
        }
    }

}
