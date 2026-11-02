package pe.oralcenter.view;

import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import pe.oralcenter.controller.*;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import pe.oralcenter.model.Usuario;
import pe.oralcenter.model.Perfil;

/** Campos, botones y mensajes de acceso. Las comprobaciones están en el controlador. */
public class AccesoVista extends JFrame {
    private AccesoControlador controlador;
    private PacientesControlador controladorPacientes;
    private OdontologosControlador controladorOdontologos;
    private CitasControlador controladorCitas;

    private static final Color AZUL = new Color(0x254E88);
    private static final Color FONDO = Interfaz.FONDO;
    final JTextField usuario = new JTextField();
    final JPasswordField clave = new JPasswordField();
    final JButton ingresar = new JButton("Iniciar sesión");
    final JButton salir = new JButton("Cerrar sesión");
    final JButton inicio = new JButton("Inicio");
    final JButton pacientes = new JButton("Pacientes");
    final JButton odontologos = new JButton("Odontólogos");
    final JButton citas = new JButton("Nueva cita");
    final JButton agenda = new JButton("Agenda");
    final JLabel mensaje = new JLabel(" ");
    private final JLabel saludo = new JLabel();
    private final CardLayout tarjetas = new CardLayout();
    private final JPanel acciones = panel(new GridLayout(0, 1, 0, 4));
    private final JPanel contenido = new JPanel(tarjetas);
    private final CardLayout navegacion = new CardLayout();
    private final JPanel paginas = new JPanel(navegacion);
    PacientesVista vistaPacientes;
    OdontologosVista vistaOdontologos;
    CitasVista vistaCitas;
    AgendaVista vistaAgenda;
    InicioVista vistaInicio;

    public AccesoVista() {
        super("Oral Center · Acceso");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(1000, 700));
        setPreferredSize(new Dimension(1120, 720));
        contenido.add(crearAcceso(), "acceso");
        contenido.add(crearSistema(), "usuario");
        setContentPane(contenido);
        for (JComponent campo : new JComponent[]{usuario, clave, ingresar, salir, inicio, pacientes, odontologos, citas, agenda, mensaje}) {
            campo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        }
        for (JButton boton : new JButton[]{ingresar, salir, inicio, pacientes, odontologos, citas, agenda}) {
            boton.setBackground(boton == ingresar ? AZUL : Color.WHITE);
            boton.setForeground(boton == ingresar ? Color.WHITE : AZUL);
            boton.setPreferredSize(new Dimension(160, 38));
        }
        mensaje.setForeground(new Color(0xA12622));
        mensaje.setVerticalAlignment(SwingConstants.TOP);
        pack();
        setLocationRelativeTo(null);
    }

    private JPanel crearAcceso() {
        JPanel acceso = panel(new BorderLayout());
        acceso.add(crearMarca(), BorderLayout.WEST);
        JPanel formulario = crearFormulario();
        formulario.setPreferredSize(new Dimension(420, 350));
        JPanel centro = panel(new GridBagLayout());
        centro.add(formulario);
        JPanel derecha = panel(new BorderLayout());
        derecha.setBorder(new EmptyBorder(40, 40, 40, 40));
        derecha.add(centro, BorderLayout.CENTER);
        acceso.add(derecha, BorderLayout.CENTER);
        return acceso;
    }

    private JPanel crearSistema() {
        JPanel sistema = panel(new BorderLayout());
        JPanel lateral = new JPanel(new BorderLayout(0, 18));
        lateral.setBackground(Color.WHITE);
        lateral.setPreferredSize(new Dimension(190, 0));
        lateral.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(0xDCE7F3)));
        JLabel nombre = texto("Oral Center", 21);
        nombre.setFont(nombre.getFont().deriveFont(Font.BOLD));
        nombre.setForeground(Color.WHITE);
        nombre.setOpaque(true);
        nombre.setBackground(AZUL);
        nombre.setBorder(new EmptyBorder(26, 18, 26, 18));
        lateral.add(nombre, BorderLayout.NORTH);
        acciones.setOpaque(false);
        JPanel menu = Interfaz.panel(new BorderLayout());
        menu.setBorder(new EmptyBorder(0, 12, 0, 12));
        menu.add(acciones, BorderLayout.NORTH);
        lateral.add(menu, BorderLayout.CENTER);
        JPanel pieMenu = Interfaz.panel(new BorderLayout());
        pieMenu.setBorder(new EmptyBorder(12, 18, 24, 18));
        salir.setBorder(new EmptyBorder(8, 0, 8, 0));
        salir.setHorizontalAlignment(SwingConstants.LEFT);
        pieMenu.add(salir);
        lateral.add(pieMenu, BorderLayout.SOUTH);
        sistema.add(lateral, BorderLayout.WEST);

        JPanel cabecera = new JPanel(new BorderLayout());
        cabecera.setBackground(Color.WHITE);
        cabecera.setPreferredSize(new Dimension(0, 52));
        cabecera.setBorder(new EmptyBorder(12, 24, 12, 24));
        saludo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        saludo.setForeground(new Color(0x52677F));
        saludo.putClientProperty("html.disable", true);
        cabecera.add(saludo, BorderLayout.EAST);
        JPanel cuerpo = panel(new BorderLayout());
        cuerpo.add(cabecera, BorderLayout.NORTH);
        paginas.add(crearBienvenida(), "inicio");
        cuerpo.add(paginas, BorderLayout.CENTER);
        sistema.add(cuerpo, BorderLayout.CENTER);
        return sistema;
    }

    private JPanel crearMarca() {
        JPanel marca = new JPanel(new GridBagLayout());
        marca.setBackground(AZUL);
        marca.setPreferredSize(new Dimension(380, 0));
        JLabel nombre = texto("<html><h1>Oral Center</h1></html>", 16);
        nombre.setForeground(Color.WHITE);
        marca.add(nombre);
        return marca;
    }

    private JPanel crearFormulario() {
        JPanel formulario = Interfaz.tarjeta(new BorderLayout(0, 20));
        formulario.add(Interfaz.titulo("Iniciar sesión"), BorderLayout.NORTH);
        JPanel campos = Interfaz.panel(new GridLayout(2, 1, 0, 16));
        campos.add(Interfaz.campo("Usuario", usuario));
        campos.add(Interfaz.campo("Contraseña", clave));
        formulario.add(campos, BorderLayout.CENTER);
        JPanel acciones = Interfaz.panel(new BorderLayout(0, 12));
        acciones.add(ingresar, BorderLayout.NORTH);
        mensaje.setPreferredSize(new Dimension(400, 54));
        acciones.add(mensaje, BorderLayout.CENTER);
        formulario.add(acciones, BorderLayout.SOUTH);
        return formulario;
    }

    private JPanel crearBienvenida() {
        vistaInicio = new InicioVista();
        vistaInicio.nuevaCita.addActionListener(e -> citas.doClick());
        vistaInicio.verAgenda.addActionListener(e -> agenda.doClick());
        vistaInicio.verPacientes.addActionListener(e -> pacientes.doClick());
        vistaInicio.actualizar.addActionListener(e -> vistaInicio.cargar(controlador, controladorPacientes, controladorCitas));
        return vistaInicio;
    }

    public void trabajando(boolean ocupado) {
        usuario.setEnabled(!ocupado);
        clave.setEnabled(!ocupado);
        ingresar.setEnabled(!ocupado);
    }

    public void mostrarError(String error) {
        mensaje.setText("<html>" + error.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;") + "</html>");
        clave.setText("");
        clave.requestFocusInWindow();
    }

    public void mostrarUsuario(Usuario actual) {
        saludo.setText(actual.getNombreUsuario());
        boolean general = actual.getPerfiles().contains(Perfil.RECEPCIONISTA) || actual.getPerfiles().contains(Perfil.ADMINISTRADOR);
        pacientes.setVisible(general || actual.getPerfiles().contains(Perfil.ODONTOLOGO));
        pacientes.setText(general ? "Pacientes" : "Mis pacientes");
        odontologos.setVisible(actual.getPerfiles().contains(Perfil.ADMINISTRADOR));
        acciones.removeAll();
        for (JButton boton : new JButton[]{inicio, pacientes, odontologos, citas, agenda}) {
            if (boton.isVisible()) { acciones.add(boton); }
        }
        acciones.revalidate();
        clave.setText("");
        tarjetas.show(contenido, "usuario");
        mostrarSeccion("inicio", inicio);
        ajustarTamano(1320, 760);
        inicio.requestFocusInWindow();
    }

    public void mostrarAcceso() {
        vistaInicio.cancelarCarga();
        saludo.setText("");
        clave.setText("");
        mensaje.setText(" ");
        paginas.removeAll();
        paginas.add(crearBienvenida(), "inicio");
        vistaPacientes = null;
        vistaOdontologos = null;
        vistaCitas = null;
        vistaAgenda = null;
        tarjetas.show(contenido, "acceso");
        setTitle("Oral Center · Acceso");
        ajustarTamano(1120, 720);
        usuario.requestFocusInWindow();
    }

    private void ajustarTamano(int ancho, int alto) {
        Rectangle pantalla = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        setSize(Math.min(ancho, pantalla.width), Math.min(alto, pantalla.height));
        setLocationRelativeTo(null);
    }

    private void mostrarSeccion(String nombre, JButton elegido) {
        navegacion.show(paginas, nombre);
        if (nombre.equals("inicio")) { vistaInicio.cargar(controlador, controladorPacientes, controladorCitas); }
        for (JButton boton : new JButton[]{inicio, pacientes, odontologos, citas, agenda}) {
            boton.setBackground(boton == elegido ? new Color(0xE7EFFA) : Color.WHITE);
            boton.setForeground(boton == elegido ? AZUL : new Color(0x333333));
            boton.setBorder(new EmptyBorder(9, 13, 9, 10));
            boton.setHorizontalAlignment(SwingConstants.LEFT);
        }
        setTitle("Oral Center · " + elegido.getText());
    }

    void navegacionDisponible(boolean disponible) {
        for (JButton boton : new JButton[]{inicio, pacientes, odontologos, citas, agenda, salir}) {
            boton.setEnabled(disponible);
        }
        setDefaultCloseOperation(disponible ? DISPOSE_ON_CLOSE : DO_NOTHING_ON_CLOSE);
    }

    private static JPanel panel(LayoutManager distribucion) {
        JPanel panel = new JPanel(distribucion);
        panel.setBackground(FONDO);
        return panel;
    }

    private static JLabel texto(String texto, int tamano) {
        JLabel etiqueta = new JLabel(texto);
        etiqueta.setFont(new Font("Segoe UI", Font.PLAIN, tamano));
        return etiqueta;
    }

    // Acciones de los botones de esta ventana.
    public void iniciar(AccesoControlador acceso, PacientesControlador controlPacientes,
            OdontologosControlador controlOdontologos, CitasControlador controlCitas) {
        controlador = acceso;
        controladorPacientes = controlPacientes;
        controladorOdontologos = controlOdontologos;
        controladorCitas = controlCitas;

        ingresar.addActionListener(e -> ingresar());
        salir.addActionListener(e -> salir());
        inicio.addActionListener(e -> mostrarSeccion("inicio", inicio));
        pacientes.addActionListener(e -> abrirPacientes());
        odontologos.addActionListener(e -> abrirOdontologos());
        citas.addActionListener(e -> abrirCitas());
        agenda.addActionListener(e -> abrirAgenda());
    }

    private void ingresar() {
        String nombreUsuario = usuario.getText();
        char[] claveIngresada = clave.getPassword();
        trabajando(true);

        // La contraseña se verifica fuera del hilo de la ventana para no congelarla.
        new SwingWorker<Usuario, Void>() {
            @Override
            protected Usuario doInBackground() {
                try {
                    return controlador.iniciarSesion(nombreUsuario, claveIngresada);
                } finally {
                    Arrays.fill(claveIngresada, '\0');
                }
            }

            @Override
            protected void done() {
                trabajando(false);
                try {
                    mostrarUsuario(get());
                } catch (InterruptedException | ExecutionException ex) {
                    mostrarError(Interfaz.error(ex));
                }
            }
        }.execute();
    }
    private void salir() {
        controlador.cerrarSesion();
        mostrarAcceso();
    }

    private void abrirPacientes() {
        if (vistaPacientes == null) {
            vistaPacientes = new PacientesVista();
            paginas.add(vistaPacientes, "pacientes");
            vistaPacientes.volver.addActionListener(e -> mostrarSeccion("inicio", inicio));
            vistaPacientes.iniciar(controladorPacientes);
        } else { vistaPacientes.buscar.doClick(); }
        mostrarSeccion("pacientes", pacientes);
    }

    private void abrirOdontologos() {
        if (vistaOdontologos == null) {
            vistaOdontologos = new OdontologosVista();
            paginas.add(vistaOdontologos, "odontologos");
            vistaOdontologos.volver.addActionListener(e -> mostrarSeccion("inicio", inicio));
            vistaOdontologos.iniciar(controladorOdontologos);
        } else { vistaOdontologos.actualizar.doClick(); }
        mostrarSeccion("odontologos", odontologos);
    }

    private void abrirCitas() {
        if (vistaCitas == null) {
            vistaCitas = new CitasVista();
            paginas.add(vistaCitas, "citas");
            vistaCitas.volver.addActionListener(e -> mostrarSeccion("inicio", inicio));
            vistaCitas.iniciar(controladorCitas);
        } else { vistaCitas.actualizar.doClick(); }
        mostrarSeccion("citas", citas);
    }

    private void abrirAgenda() {
        if (vistaAgenda == null) {
            vistaAgenda = new AgendaVista();
            paginas.add(vistaAgenda, "agenda");
            vistaAgenda.volver.addActionListener(e -> mostrarSeccion("inicio", inicio));
            vistaAgenda.iniciar(controladorCitas);
        } else { vistaAgenda.consultar.doClick(); }
        mostrarSeccion("agenda", agenda);
    }
}
