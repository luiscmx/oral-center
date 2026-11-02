package pe.oralcenter.view;

import java.util.concurrent.ExecutionException;
import pe.oralcenter.controller.PacientesControlador;

import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import pe.oralcenter.model.Paciente;

/** Formulario de registro y tabla de pacientes. */
public class PacientesVista extends JPanel {
    private PacientesControlador controlador;
    private long idEdicion;
    private boolean permiteGestion = true;
    final JPanel formulario;
    final JPanel accionesGestion;
    final JTabbedPane contenido = new JTabbedPane();
    private final JLabel nombrePaciente = new JLabel("Pacientes registrados");
    private final JLabel documentoPaciente = new JLabel(" ");
    private final JLabel avatar = new JLabel("OC", SwingConstants.CENTER);
    final JButton nuevo = new JButton("Nuevo paciente");

    final JTextField dni = new JTextField();
    final JTextField nombres = new JTextField();
    final JTextField apellidos = new JTextField();
    final JTextField telefono = new JTextField();
    final JTextField busqueda = new JTextField();
    final JButton guardar = new JButton("Guardar paciente");
    final JButton limpiar = new JButton("Limpiar");
    final JButton buscar = new JButton("Buscar");
    final JButton verTodos = new JButton("Ver todos");
    final JButton editar = new JButton("Editar seleccionado");
    final JButton eliminar = new JButton("Eliminar seleccionado");
    final JButton volver = new JButton("Volver");
    final JTextArea mensaje = new JTextArea(2, 1);
    private final JLabel tituloFormulario = new JLabel("Registrar paciente");
    private final DefaultTableModel datos = new DefaultTableModel(
            new String[]{"Código", "DNI", "Nombres", "Apellidos", "Teléfono"}, 0) {
        @Override public boolean isCellEditable(int fila, int columna) { return false; }
    };
    final JTable tabla = new JTable(datos);

    public PacientesVista() {
        super(new BorderLayout());
        JPanel principal = Interfaz.principal("Gestión de pacientes", mensaje, volver);
        JPanel listado = Interfaz.tarjeta(new BorderLayout(0, 16));
        JPanel encabezado = Interfaz.panel(new BorderLayout(12, 0));
        encabezado.add(Interfaz.campo("Buscar por DNI o nombre", busqueda), BorderLayout.CENTER);
        JPanel accionesBusqueda = Interfaz.panel(new BorderLayout());
        accionesBusqueda.add(Interfaz.botones(buscar, verTodos, nuevo), BorderLayout.SOUTH);
        encabezado.add(accionesBusqueda, BorderLayout.EAST);
        listado.add(encabezado, BorderLayout.NORTH);
        Interfaz.tabla(tabla, "Pacientes registrados", 70, 110, 220, 240, 150);
        listado.add(new JScrollPane(tabla), BorderLayout.CENTER);
        editar.setText("Editar paciente");
        eliminar.setText("Eliminar paciente");
        accionesGestion = Interfaz.botones(editar, eliminar);
        listado.add(accionesGestion, BorderLayout.SOUTH);

        JPanel campos = Interfaz.panel(new GridLayout(2, 1, 0, 16));
        campos.add(Interfaz.fila(Interfaz.campo("DNI", dni), Interfaz.campo("Nombres", nombres)));
        campos.add(Interfaz.fila(Interfaz.campo("Apellidos", apellidos), Interfaz.campo("Teléfono", telefono)));
        JPanel ficha = Interfaz.tarjeta(new BorderLayout(0, 20));
        tituloFormulario.setFont(new Font("Segoe UI", Font.BOLD, 18));
        ficha.add(tituloFormulario, BorderLayout.NORTH);
        ficha.add(campos, BorderLayout.CENTER);
        ficha.add(Interfaz.botones(guardar, limpiar), BorderLayout.SOUTH);
        formulario = Interfaz.panel(new BorderLayout());
        formulario.setOpaque(true);
        formulario.setBackground(Color.WHITE);
        formulario.add(ficha, BorderLayout.NORTH);
        limpiar.setText("Cancelar");
        contenido.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        contenido.setBackground(Color.WHITE);
        contenido.addTab("Pacientes registrados", listado);
        contenido.addTab("Datos personales", formulario);
        JPanel fichaPaciente = Interfaz.tarjeta(new BorderLayout(14, 0));
        avatar.setOpaque(true);
        avatar.setBackground(new Color(0xE7F3FF));
        avatar.setForeground(Interfaz.AZUL);
        avatar.setFont(new Font("Segoe UI", Font.BOLD, 18));
        avatar.setPreferredSize(new Dimension(48, 48));
        fichaPaciente.add(avatar, BorderLayout.WEST);
        nombrePaciente.setFont(new Font("Segoe UI", Font.BOLD, 17));
        nombrePaciente.setForeground(Interfaz.AZUL);
        nombrePaciente.putClientProperty("html.disable", true);
        documentoPaciente.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        documentoPaciente.setForeground(new Color(0x52677F));
        JPanel identidad = Interfaz.panel(new GridLayout(2, 1, 0, 5));
        identidad.add(nombrePaciente);
        identidad.add(documentoPaciente);
        fichaPaciente.add(identidad, BorderLayout.CENTER);
        JPanel centro = Interfaz.panel(new BorderLayout(0, 18));
        centro.add(fichaPaciente, BorderLayout.NORTH);
        centro.add(contenido, BorderLayout.CENTER);
        principal.add(centro, BorderLayout.CENTER);
        Interfaz.primario(nuevo);
        Interfaz.primario(guardar);
        tabla.getSelectionModel().addListSelectionListener(e -> {
            Paciente seleccionado = getPacienteSeleccionado();
            if (!e.getValueIsAdjusting() && seleccionado != null && idEdicion == 0) { mostrarIdentidad(seleccionado); }
        });
        nuevo.addActionListener(e -> {
            contenido.setSelectedIndex(1);
            if (idEdicion == 0) {
                nombrePaciente.setText("Nuevo paciente");
                documentoPaciente.setText(" ");
                avatar.setText("NP");
            }
            dni.requestFocusInWindow();
        });
        eliminar.setForeground(new Color(0xA12622));
        add(principal, BorderLayout.CENTER);
    }

    public void mostrarPacientes(List<Paciente> pacientes) {
        datos.setRowCount(0);
        for (Paciente paciente : pacientes) {
            datos.addRow(new Object[]{paciente.getId(), paciente.getDni(), paciente.getNombres(),
                    paciente.getApellidos(), paciente.getTelefono()});
        }
    }

    public void limpiarFormulario() {
        dni.setText("");
        nombres.setText("");
        apellidos.setText("");
        telefono.setText("");
        tituloFormulario.setText("Registrar paciente");
        guardar.setText("Guardar paciente");
        limpiar.setText("Cancelar");
        contenido.setSelectedIndex(0);
        tabla.clearSelection();
        nombrePaciente.setText("Pacientes registrados");
        documentoPaciente.setText(" ");
        avatar.setText("OC");
        busqueda.requestFocusInWindow();
    }

    public Paciente getPacienteSeleccionado() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            return null;
        }
        return new Paciente((long) tabla.getValueAt(fila, 0), (String) tabla.getValueAt(fila, 1),
                (String) tabla.getValueAt(fila, 2), (String) tabla.getValueAt(fila, 3),
                (String) tabla.getValueAt(fila, 4));
    }

    public boolean tieneDatosEnFormulario() {
        return !dni.getText().isBlank() || !nombres.getText().isBlank()
                || !apellidos.getText().isBlank() || !telefono.getText().isBlank();
    }

    public boolean confirmarEliminacion(Paciente paciente) {
        String texto = "¿Eliminar al paciente con código " + paciente.getId() + " y DNI " + paciente.getDni() + "?"
                + "\nTambién se eliminarán todas sus citas asociadas."
                + "\nEsta acción no se puede deshacer.";
        String[] opciones = {"Eliminar", "Cancelar"};
        int opcion = JOptionPane.showOptionDialog(this, texto, "Eliminar paciente",
                JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, opciones, opciones[1]);
        return opcion == 0;
    }

    private void mostrarIdentidad(Paciente paciente) {
        nombrePaciente.setText(paciente.getNombres() + " " + paciente.getApellidos());
        documentoPaciente.setText("DNI " + paciente.getDni() + "   ·   Código " + paciente.getId());
        String iniciales = "";
        if (!paciente.getNombres().isBlank()) { iniciales += paciente.getNombres().substring(0, 1); }
        if (!paciente.getApellidos().isBlank()) { iniciales += paciente.getApellidos().substring(0, 1); }
        avatar.setText(iniciales.toUpperCase(java.util.Locale.ROOT));
    }

    public void mostrarEdicion(Paciente paciente) {
        mostrarIdentidad(paciente);
        dni.setText(paciente.getDni());
        nombres.setText(paciente.getNombres());
        apellidos.setText(paciente.getApellidos());
        telefono.setText(paciente.getTelefono());
        contenido.setSelectedIndex(1);
        tituloFormulario.setText("Editar paciente");
        guardar.setText("Guardar cambios");
        limpiar.setText("Cancelar edición");
        dni.requestFocusInWindow();
    }

    public void trabajando(boolean ocupado) {
        for (JComponent campo : new JComponent[]{busqueda, buscar, verTodos, volver}) {
            campo.setEnabled(!ocupado);
        }
        for (JComponent campo : new JComponent[]{dni, nombres, apellidos, telefono, guardar, limpiar, editar, eliminar, nuevo}) {
            campo.setEnabled(!ocupado && permiteGestion);
        }
        Interfaz.navegacion(this, ocupado);
    }


    // Acciones de los botones de esta ventana.
    public void iniciar(PacientesControlador controlador) {
        this.controlador = controlador;
        permiteGestion = controlador.puedeGestionar();
        formulario.setVisible(false);
        accionesGestion.setVisible(permiteGestion);
        nuevo.setVisible(permiteGestion);
        if (!permiteGestion) {
            contenido.remove(formulario);
            formulario.setVisible(false);
        }
        contenido.setSelectedIndex(0);

        guardar.addActionListener(e -> guardar());
        buscar.addActionListener(e -> cargar(""));
        verTodos.addActionListener(e -> {
            busqueda.setText("");
            cargar("");
        });
        editar.addActionListener(e -> editar());
        eliminar.addActionListener(e -> eliminar());
        limpiar.addActionListener(e -> {
            idEdicion = 0;
            limpiarFormulario();
            Interfaz.mensaje(mensaje, "", false);
        });
        cargar("");
    }

    private void guardar() {
        long id = idEdicion;
        String documento = dni.getText();
        String nombreIngresado = nombres.getText();
        String apellidoIngresado = apellidos.getText();
        String telefonoIngresado = telefono.getText();
        trabajando(true);
        Interfaz.mensaje(mensaje, "Guardando paciente...", false);
        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() {
                if (id == 0) {
                    controlador.registrar(documento, nombreIngresado, apellidoIngresado, telefonoIngresado);
                } else {
                    controlador.actualizar(id, documento, nombreIngresado, apellidoIngresado, telefonoIngresado);
                }
                return null;
            }

            @Override protected void done() {
                trabajando(false);
                try {
                    get();
                    idEdicion = 0;
                    limpiarFormulario();
                    busqueda.setText("");
                    if (id == 0) {
                        cargar("Paciente guardado correctamente.");
                    } else {
                        cargar("Datos del paciente actualizados correctamente.");
                    }
                } catch (InterruptedException | ExecutionException ex) {
                    Interfaz.mensaje(mensaje, Interfaz.error(ex), true);
                }
            }
        }.execute();
    }

    private void cargar(String aviso) {
        String filtro = busqueda.getText();
        if (!permiteGestion) { mostrarPacientes(List.of()); }
        trabajando(true);
        Interfaz.mensaje(mensaje, "Consultando pacientes...", false);
        new SwingWorker<List<Paciente>, Void>() {
            @Override protected List<Paciente> doInBackground() {
                return controlador.buscar(filtro);
            }

            @Override protected void done() {
                trabajando(false);
                try {
                    List<Paciente> lista = get();
                    mostrarPacientes(lista);
                    String texto = aviso;
                    if (texto.isEmpty() && lista.isEmpty()) {
                        texto = "No hay pacientes registrados.";
                        if (!permiteGestion) { texto = "No hay pacientes asociados a tus citas."; }
                        if (!filtro.isBlank()) {
                            texto = "No se encontraron pacientes. Prueba otro DNI o nombre, o pulsa Ver todos.";
                        }
                    }
                    Interfaz.mensaje(mensaje, texto, false);
                    if (permiteGestion) { dni.requestFocusInWindow(); }
                    else { busqueda.requestFocusInWindow(); }
                } catch (InterruptedException | ExecutionException ex) {
                    // El guardado puede haber terminado aunque falle la actualización de la tabla.
                    Interfaz.mensaje(mensaje, aviso + " " + Interfaz.error(ex) + " Pulsa Buscar para consultar de nuevo.", true);
                }
            }
        }.execute();
    }

    private void editar() {
        if (idEdicion > 0) {
            Interfaz.mensaje(mensaje, "Guarda o cancela la edición actual antes de elegir otro paciente.", true);
            return;
        }
        Paciente paciente = getPacienteSeleccionado();
        if (paciente == null) {
            Interfaz.mensaje(mensaje, "Selecciona un paciente de la tabla para editarlo.", true);
            return;
        }
        if (tieneDatosEnFormulario()) {
            Interfaz.mensaje(mensaje, "Pulsa Limpiar antes de cargar un paciente para editarlo.", true);
            return;
        }
        idEdicion = paciente.getId();
        mostrarEdicion(paciente);
        Interfaz.mensaje(mensaje, "Modifica los datos y pulsa Guardar cambios.", false);
    }


    private void eliminar() {
        if (idEdicion > 0) {
            Interfaz.mensaje(mensaje, "Guarda o cancela la edición actual antes de eliminar un paciente.", true);
            return;
        }
        if (tieneDatosEnFormulario()) {
            Interfaz.mensaje(mensaje, "Guarda o limpia el formulario antes de eliminar un paciente.", true);
            return;
        }
        Paciente paciente = getPacienteSeleccionado();
        if (paciente == null) {
            Interfaz.mensaje(mensaje, "Selecciona un paciente de la tabla para eliminarlo.", true);
            return;
        }
        if (!confirmarEliminacion(paciente)) {
            Interfaz.mensaje(mensaje, "Eliminación cancelada.", false);
            return;
        }
        trabajando(true);
        Interfaz.mensaje(mensaje, "Eliminando paciente...", false);
        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() {
                controlador.eliminar(paciente.getId());
                return null;
            }

            @Override protected void done() {
                trabajando(false);
                try {
                    get();
                    limpiarFormulario();
                    cargar("Paciente eliminado correctamente.");
                } catch (InterruptedException | ExecutionException ex) {
                    Interfaz.mensaje(mensaje, Interfaz.error(ex), true);
                }
            }
        }.execute();
    }
}
