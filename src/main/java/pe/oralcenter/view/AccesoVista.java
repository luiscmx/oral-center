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
    private static final Color FONDO = new Color(0xF5F7FA);
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

    public AccesoVista() {
        super("Oral Center · Acceso");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(1000, 650));
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
        formulario.setPreferredSize(new Dimension(400, 350));
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
        JPanel lateral = new JPanel(new BorderLayout(0, 24));
        lateral.setBackground(Color.WHITE);
        lateral.setPreferredSize(new Dimension(190, 0));
        lateral.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(0xD8DEE8)),
                new EmptyBorder(24, 12, 24, 12)));
        JLabel nombre = texto("Oral Center", 20);
        nombre.setFont(nombre.getFont().deriveFont(Font.BOLD));
        nombre.setForeground(AZUL);
        nombre.setBorder(new EmptyBorder(0, 8, 8, 0));
        lateral.add(nombre, BorderLayout.NORTH);
        acciones.setOpaque(false);
        JPanel menu = Interfaz.panel(new BorderLayout());
        menu.add(acciones, BorderLayout.NORTH);
        lateral.add(menu, BorderLayout.CENTER);
        lateral.add(salir, BorderLayout.SOUTH);
        sistema.add(lateral, BorderLayout.WEST);
        paginas.add(crearBienvenida(), "inicio");
        sistema.add(paginas, BorderLayout.CENTER);
        return sistema;
    }

    private JPanel crearMarca() {
        JPanel marca = new JPanel(new GridBagLayout());
        marca.setBackground(AZUL);
        marca.setPreferredSize(new Dimension(300, 0));
        JLabel nombre = texto("<html><h1>Oral Center</h1></html>", 16);
        nombre.setForeground(Color.WHITE);
        marca.add(nombre);
        return marca;
    }

    private JPanel crearFormulario() {
        JPanel formulario = panel(new BorderLayout(0, 20));
        JPanel encabezado = panel(new GridLayout(2, 1, 0, 8));
        encabezado.add(Interfaz.titulo("Iniciar sesión"));
        encabezado.add(texto("Accede con tu usuario y contraseña.", 16));
        formulario.add(encabezado, BorderLayout.NORTH);
        JPanel campos = panel(new GridLayout(2, 1, 0, 16));
        campos.add(Interfaz.campo("Usuario", usuario));
        campos.add(Interfaz.campo("Contraseña", clave));
        formulario.add(campos, BorderLayout.CENTER);
        JPanel acciones = panel(new BorderLayout(0, 12));
        acciones.add(ingresar, BorderLayout.NORTH);
        mensaje.setPreferredSize(new Dimension(400, 54));
        acciones.add(mensaje, BorderLayout.CENTER);
        formulario.add(acciones, BorderLayout.SOUTH);
        return formulario;
    }

    private JPanel crearBienvenida() {
        JPanel bienvenida = panel(new BorderLayout(0, 24));
        bienvenida.setBorder(new EmptyBorder(24, 24, 24, 24));
        bienvenida.add(Interfaz.titulo("Inicio"), BorderLayout.NORTH);
        saludo.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        JPanel centro = Interfaz.panel(new BorderLayout(0, 20));
        centro.add(saludo, BorderLayout.NORTH);
        JPanel accesos = Interfaz.panel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        JButton nuevaCita = new JButton("Nueva cita");
        JButton verAgenda = new JButton("Ver agenda");
        JButton verPacientes = new JButton("Pacientes");
        accesos.add(Interfaz.botones(nuevaCita, verAgenda, verPacientes));
        centro.add(accesos, BorderLayout.CENTER);
        JPanel cuerpo = Interfaz.panel(new BorderLayout());
        cuerpo.add(centro, BorderLayout.NORTH);
        bienvenida.add(cuerpo, BorderLayout.CENTER);
        nuevaCita.addActionListener(e -> citas.doClick());
        verAgenda.addActionListener(e -> agenda.doClick());
        verPacientes.addActionListener(e -> pacientes.doClick());
        return bienvenida;
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
        saludo.setText("Hola, " + actual.getNombreUsuario());
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
        for (JButton boton : new JButton[]{inicio, pacientes, odontologos, citas, agenda}) {
            boton.setBackground(boton == elegido ? new Color(0xE7EFFA) : Color.WHITE);
            boton.setForeground(boton == elegido ? AZUL : new Color(0x333333));
            boton.setBorder(new EmptyBorder(9, 12, 9, 12));
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
