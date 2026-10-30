package pe.oralcenter;

import javax.swing.SwingUtilities;
import javax.swing.JOptionPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pe.oralcenter.controller.AccesoControlador;
import pe.oralcenter.dao.Conexion;
import pe.oralcenter.dao.PacienteMySQLDAO;
import pe.oralcenter.dao.OdontologoMySQLDAO;
import pe.oralcenter.dao.CitaMySQLDAO;
import pe.oralcenter.dao.UsuarioMySQLDAO;
import pe.oralcenter.controller.PacientesControlador;
import pe.oralcenter.controller.OdontologosControlador;
import pe.oralcenter.controller.CitasControlador;
import pe.oralcenter.view.AccesoVista;

/** Punto de entrada de la primera versión de escritorio de Oral Center. */
public class Aplicacion {
    private static final Logger REGISTRO = LoggerFactory.getLogger(Aplicacion.class);
    public static void main(String[] args) {
        try {
            Conexion conexion = new Conexion();
            UsuarioMySQLDAO usuarios = new UsuarioMySQLDAO(conexion);
            AccesoControlador acceso = new AccesoControlador(usuarios);
            PacientesControlador pacientes = new PacientesControlador(new PacienteMySQLDAO(conexion), acceso);
            OdontologosControlador odontologos = new OdontologosControlador(new OdontologoMySQLDAO(conexion), acceso);
            CitasControlador citas = new CitasControlador(new CitaMySQLDAO(conexion), acceso);
            SwingUtilities.invokeLater(() -> {
                AccesoVista vista = new AccesoVista();
                vista.iniciar(acceso, pacientes, odontologos, citas);
                vista.setVisible(true);
                REGISTRO.info("Aplicación iniciada.");
            });
        } catch (IllegalStateException ex) {
            REGISTRO.error("No se pudo iniciar la aplicación.");
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Conexión de Oral Center", JOptionPane.ERROR_MESSAGE);
        }
    }
}
