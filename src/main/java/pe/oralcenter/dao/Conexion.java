package pe.oralcenter.dao;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Lee la configuración local y abre conexiones con MySQL. */
public class Conexion {
    private static final Logger REGISTRO = LoggerFactory.getLogger(Conexion.class);
    private final Properties datos = new Properties();

    public Conexion() {
        try (Reader archivo = Files.newBufferedReader(Path.of("conexion.properties"), StandardCharsets.UTF_8)) {
            datos.load(archivo);
        } catch (IOException ex) {
            REGISTRO.error("No se pudo leer la configuración de MySQL.");
            throw new IllegalStateException("Configura el archivo conexion.properties para conectar con MySQL.", ex);
        }
        if (datos.getProperty("url") == null || datos.getProperty("usuario") == null || datos.getProperty("clave") == null) {
            REGISTRO.error("La configuración de MySQL está incompleta.");
            throw new IllegalStateException("Completa url, usuario y clave en conexion.properties.");
        }
    }

    public Connection abrir() throws SQLException {
        try {
            return DriverManager.getConnection(datos.getProperty("url"),
                    datos.getProperty("usuario"), datos.getProperty("clave"));
        } catch (SQLException ex) {
            REGISTRO.error("Falló la conexión con MySQL (código SQL {}).", ex.getErrorCode());
            throw ex;
        }
    }
}
