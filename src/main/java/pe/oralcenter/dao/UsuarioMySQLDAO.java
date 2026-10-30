package pe.oralcenter.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pe.oralcenter.model.Perfil;
import pe.oralcenter.model.Usuario;

/** Busca la cuenta y sus perfiles en las tablas del diseño de Oral Center. */
public class UsuarioMySQLDAO implements UsuarioDAO {
    private static final Logger REGISTRO = LoggerFactory.getLogger(UsuarioMySQLDAO.class);
    private final Conexion conexion;

    public UsuarioMySQLDAO(Conexion conexion) {
        this.conexion = conexion;
    }

    @Override
    public Usuario buscarPorNombre(String nombreUsuario) {
        String sql = "SELECT u.id_usuario, u.nombre_usuario, u.clave_hash, u.id_odontologo, p.codigo "
                + "FROM usuario u LEFT JOIN usuario_perfil up ON u.id_usuario = up.id_usuario "
                + "LEFT JOIN perfil p ON up.id_perfil = p.id_perfil WHERE u.nombre_usuario = ?";
        try (Connection cn = conexion.abrir(); PreparedStatement consulta = cn.prepareStatement(sql)) {
            consulta.setString(1, nombreUsuario);
            try (ResultSet filas = consulta.executeQuery()) {
                if (!filas.next()) {
                    return null;
                }
                long id = filas.getLong("id_usuario");
                String nombre = filas.getString("nombre_usuario");
                String hash = filas.getString("clave_hash");
                Long odontologo = filas.getObject("id_odontologo", Long.class);
                Set<Perfil> perfiles = new HashSet<>();
                do {
                    String codigo = filas.getString("codigo");
                    if (codigo != null) {
                        perfiles.add(Perfil.valueOf(codigo));
                    }
                } while (filas.next());
                return new Usuario(id, nombre, hash, perfiles, odontologo);
            }
        } catch (SQLException ex) {
            REGISTRO.error("Falló la consulta de cuentas (código SQL {}).", ex.getErrorCode());
            throw new IllegalStateException("No se pudo consultar MySQL. Revisa la conexión y que la base esté preparada.", ex);
        }
    }
}
