package pe.oralcenter.view;

import java.util.concurrent.ExecutionException;
import pe.oralcenter.controller.OdontologosControlador;

import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import pe.oralcenter.model.Odontologo;

/** Datos del odontólogo; los turnos se abren desde el registro seleccionado. */
public class OdontologosVista extends JPanel {
    private OdontologosControlador controlador;
    private long idEdicion;
    private final CardLayout secciones = new CardLayout();
    HorariosVista vistaHorarios;
    private final CardLayout datosVisibles = new CardLayout();
    private final JPanel contenido = new JPanel(datosVisibles);
    final JButton nuevo = new JButton("Nuevo odontólogo");

    final JTextField dni = new JTextField();
    final JTextField nombre = new JTextField();
    final JTextField especialidad = new JTextField();
    final JButton guardar = new JButton("Guardar odontólogo");
    final JButton limpiar = new JButton("Limpiar");
    final JButton editar = new JButton("Editar seleccionado");
    final JButton horarios = new JButton("Horarios");
    final JButton actualizar = new JButton("Actualizar listado");
    final JButton volver = new JButton("Volver");
    private final JLabel tituloFormulario = new JLabel("Registrar odontólogo");
    final JTextArea mensaje = new JTextArea(3, 1);
    private final DefaultTableModel datos = new DefaultTableModel(new String[]{"Código", "DNI", "Nombre completo", "Especialidad"}, 0) {
        @Override public boolean isCellEditable(int fila, int columna) { return false; }
    };
    final JTable tabla = new JTable(datos);

    public OdontologosVista() {
        setLayout(secciones);
        JPanel principal = Interfaz.principal("Odontólogos", mensaje, volver);
        JPanel listado = Interfaz.panel(new BorderLayout(0, 16));
        listado.add(Interfaz.botones(nuevo, actualizar), BorderLayout.NORTH);
        Interfaz.tabla(tabla, "Odontólogos registrados", 70, 110, 300, 260);
        listado.add(new JScrollPane(tabla), BorderLayout.CENTER);
        editar.setText("Editar odontólogo");
        horarios.setText("Gestionar horarios");
        listado.add(Interfaz.botones(editar, horarios), BorderLayout.SOUTH);

        JPanel campos = Interfaz.panel(new GridLayout(2, 1, 0, 16));
        campos.add(Interfaz.fila(Interfaz.campo("DNI", dni), Interfaz.campo("Nombre completo", nombre)));
        campos.add(Interfaz.campo("Especialidad", especialidad));
        JPanel ficha = Interfaz.panel(new BorderLayout(0, 20));
        tituloFormulario.setFont(new Font("Segoe UI", Font.BOLD, 18));
        ficha.add(tituloFormulario, BorderLayout.NORTH);
        ficha.add(campos, BorderLayout.CENTER);
        ficha.add(Interfaz.botones(guardar, limpiar), BorderLayout.SOUTH);
        JPanel formulario = Interfaz.panel(new BorderLayout());
        formulario.add(ficha, BorderLayout.NORTH);
        limpiar.setText("Cancelar");
        contenido.setOpaque(false);
        contenido.add(listado, "listado");
        contenido.add(formulario, "formulario");
        principal.add(contenido, BorderLayout.CENTER);
        nuevo.addActionListener(e -> {
            datosVisibles.show(contenido, "formulario");
            dni.requestFocusInWindow();
        });
        add(principal, "odontologos");
    }

    public void mostrarOdontologos(List<Odontologo> lista) {
        datos.setRowCount(0);
        for (Odontologo dato : lista) {
            datos.addRow(new Object[]{dato.getId(), dato.getDni(), dato.getNombreCompleto(), dato.getEspecialidad()});
        }
    }

    public Odontologo getSeleccionado() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            return null;
        }
        return new Odontologo((long) tabla.getValueAt(fila, 0), (String) tabla.getValueAt(fila, 1),
                (String) tabla.getValueAt(fila, 2), (String) tabla.getValueAt(fila, 3));
    }

    public boolean tieneDatosEnFormulario() {
        return !dni.getText().isBlank() || !nombre.getText().isBlank() || !especialidad.getText().isBlank();
    }

    public void mostrarEdicion(Odontologo dato) {
        dni.setText(dato.getDni());
        nombre.setText(dato.getNombreCompleto());
        especialidad.setText(dato.getEspecialidad());
        datosVisibles.show(contenido, "formulario");
        tituloFormulario.setText("Editar odontólogo");
        guardar.setText("Guardar cambios");
        limpiar.setText("Cancelar edición");
        dni.requestFocusInWindow();
    }

    public void limpiarFormulario() {
        dni.setText("");
        nombre.setText("");
        especialidad.setText("");
        tituloFormulario.setText("Registrar odontólogo");
        guardar.setText("Guardar odontólogo");
        limpiar.setText("Cancelar");
        datosVisibles.show(contenido, "listado");
        tabla.clearSelection();
        dni.requestFocusInWindow();
    }

    public void trabajando(boolean ocupado) {
        for (JComponent campo : new JComponent[]{dni, nombre, especialidad, guardar, limpiar, editar, horarios, actualizar, volver, nuevo}) {
            campo.setEnabled(!ocupado);
        }
        Interfaz.navegacion(this, ocupado);
    }


    // Acciones de los botones de esta ventana.
    public void iniciar(OdontologosControlador controlador) {
        this.controlador = controlador;

        guardar.addActionListener(e -> guardar());
        editar.addActionListener(e -> editar());
        horarios.addActionListener(e -> abrirHorarios());
        actualizar.addActionListener(e -> cargar(""));
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
        String nombreIngresado = nombre.getText();
        String especialidadIngresada = especialidad.getText();
        trabajando(true);
        Interfaz.mensaje(mensaje, "Guardando odontólogo...", false);
        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() {
                if (id == 0) {
                    controlador.registrar(documento, nombreIngresado, especialidadIngresada);
                }
                else { controlador.actualizar(id, documento, nombreIngresado, especialidadIngresada); }
                return null;
            }

            @Override protected void done() {
                trabajando(false);
                try {
                    get();
                    idEdicion = 0;
                    limpiarFormulario();
                    cargar(id == 0 ? "Odontólogo guardado correctamente." : "Datos del odontólogo actualizados correctamente.");
                } catch (InterruptedException | ExecutionException ex) {
                    Interfaz.mensaje(mensaje, Interfaz.error(ex), true);
                }
            }
        }.execute();
    }

    private void cargar(String aviso) {
        trabajando(true);
        Interfaz.mensaje(mensaje, "Consultando odontólogos...", false);
        new SwingWorker<List<Odontologo>, Void>() {
            @Override protected List<Odontologo> doInBackground() { return controlador.listar(); }
            @Override protected void done() {
                trabajando(false);
                try {
                    List<Odontologo> lista = get();
                    mostrarOdontologos(lista);
                    Interfaz.mensaje(mensaje, aviso.isEmpty() && lista.isEmpty() ? "No hay odontólogos registrados." : aviso, false);
                } catch (InterruptedException | ExecutionException ex) {
                    Interfaz.mensaje(mensaje, aviso + " " + Interfaz.error(ex) + " Pulsa Actualizar listado para consultar de nuevo.", true);
                }
            }
        }.execute();
    }

    private boolean formularioDisponible() {
        if (idEdicion > 0 || tieneDatosEnFormulario()) {
            Interfaz.mensaje(mensaje, "Guarda o limpia el formulario antes de elegir otro registro.", true);
            return false;
        }
        return true;
    }

    private void editar() {
        if (!formularioDisponible()) {
            return;
        }
        Odontologo elegido = getSeleccionado();
        if (elegido == null) {
            Interfaz.mensaje(mensaje, "Selecciona un odontólogo de la tabla para editarlo.", true);
            return;
        }
        idEdicion = elegido.getId();
        mostrarEdicion(elegido);
        Interfaz.mensaje(mensaje, "Modifica los datos y pulsa Guardar cambios.", false);
    }

    private void abrirHorarios() {
        if (!formularioDisponible()) {
            return;
        }
        Odontologo elegido = getSeleccionado();
        if (elegido == null) {
            Interfaz.mensaje(mensaje, "Selecciona un odontólogo de la tabla para ver sus horarios.", true);
            return;
        }
        if (vistaHorarios != null) { remove(vistaHorarios); }
        vistaHorarios = new HorariosVista(elegido);
        add(vistaHorarios, "horarios");
        vistaHorarios.volver.addActionListener(e -> secciones.show(this, "odontologos"));
        vistaHorarios.iniciar(controlador);
        secciones.show(this, "horarios");
    }

}
