package pe.oralcenter.view;

import java.util.concurrent.ExecutionException;
import pe.oralcenter.controller.OdontologosControlador;

import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import pe.oralcenter.model.Horario;
import pe.oralcenter.model.Odontologo;

/** Turnos semanales del odontólogo elegido. Lunes corresponde al día 1. */
public class HorariosVista extends JPanel {
    private OdontologosControlador controlador;
    private long idEdicion;
    private List<Horario> horarios = List.of();

    private static final Color AZUL = new Color(0x254E88);
    private static final Color FONDO = new Color(0xF5F7FA);
    private static final String[] DIAS = {"Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"};
    private final long idOdontologo;
    final JComboBox<String> dia = new JComboBox<>(DIAS);
    final JTextField inicio = new JTextField();
    final JTextField fin = new JTextField();
    final JButton guardar = new JButton("Guardar horario");
    final JButton limpiar = new JButton("Limpiar");
    final JButton editar = new JButton("Editar seleccionado");
    final JButton actualizar = new JButton("Actualizar listado");
    final JButton volver = new JButton("Volver");
    private final JLabel tituloFormulario = new JLabel("Registrar horario");
    final JTextArea mensaje = new JTextArea(3, 1);
    private final DefaultTableModel datos = new DefaultTableModel(new String[]{"Código", "Día", "Inicio", "Fin"}, 0) {
        @Override public boolean isCellEditable(int fila, int columna) { return false; }
    };
    final JTable tabla = new JTable(datos);

    public HorariosVista(Odontologo odontologo) {
        super(new BorderLayout());
        idOdontologo = odontologo.getId();
        JPanel principal = Interfaz.principal("Horarios", mensaje, volver);
        JTextArea titulo = new JTextArea("Horarios de " + odontologo.getNombreCompleto(), 2, 1);
        titulo.setEditable(false);
        titulo.setLineWrap(true);
        titulo.setWrapStyleWord(true);
        titulo.setBackground(FONDO);
        titulo.setForeground(AZUL);
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        principal.add(titulo, BorderLayout.NORTH);
        JPanel centro = Interfaz.panel(new BorderLayout(24, 0));
        JPanel campos = Interfaz.campos(new String[]{"Día de la semana", "Hora de inicio", "Hora de fin"}, dia, inicio, fin);
        JPanel datosHora = Interfaz.panel(new BorderLayout(0, 8));
        datosHora.add(campos, BorderLayout.CENTER);
        datosHora.add(new JLabel("Formato de 24 horas: 09:00"), BorderLayout.SOUTH);
        centro.add(Interfaz.formulario(tituloFormulario, datosHora, 360, guardar, limpiar), BorderLayout.WEST);
        JPanel listado = Interfaz.panel(new BorderLayout(0, 12));
        listado.add(actualizar, BorderLayout.NORTH);
        Interfaz.tabla(tabla, "Horarios semanales", 75, 160, 75, 75);
        listado.add(new JScrollPane(tabla), BorderLayout.CENTER);
        listado.add(editar, BorderLayout.SOUTH);
        centro.add(listado, BorderLayout.CENTER);
        principal.add(centro, BorderLayout.CENTER);
        Interfaz.fuente(actualizar, editar);
        add(principal, BorderLayout.CENTER);
    }


    public void mostrarHorarios(List<Horario> lista) {
        horarios = lista;
        datos.setRowCount(0);
        for (Horario horario : lista) {
            datos.addRow(new Object[]{horario.getId(), DIAS[horario.getDiaSemana() - 1], horario.getInicio(), horario.getFin()});
        }
    }

    public Horario getSeleccionado() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            return null;
        }
        return horarios.get(fila);
    }

    public boolean tieneDatosEnFormulario() {
        return dia.getSelectedIndex() != 0 || !inicio.getText().isBlank() || !fin.getText().isBlank();
    }

    public void mostrarEdicion(Horario horario) {
        dia.setSelectedIndex(horario.getDiaSemana() - 1);
        inicio.setText(horario.getInicio().toString());
        fin.setText(horario.getFin().toString());
        tituloFormulario.setText("Editar horario");
        guardar.setText("Guardar cambios");
        limpiar.setText("Cancelar edición");
        inicio.requestFocusInWindow();
    }

    public void limpiarFormulario() {
        dia.setSelectedIndex(0);
        inicio.setText("");
        fin.setText("");
        tituloFormulario.setText("Registrar horario");
        guardar.setText("Guardar horario");
        limpiar.setText("Limpiar");
        tabla.clearSelection();
        inicio.requestFocusInWindow();
    }

    public void trabajando(boolean ocupado) {
        for (JComponent campo : new JComponent[]{dia, inicio, fin, guardar, limpiar, editar, actualizar, volver}) {
            campo.setEnabled(!ocupado);
        }
        Interfaz.navegacion(this, ocupado);
    }


    // Acciones de los botones de esta ventana.
    public void iniciar(OdontologosControlador controlador) {
        this.controlador = controlador;

        guardar.addActionListener(e -> guardar());
        editar.addActionListener(e -> editar());
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
        int diaElegido = dia.getSelectedIndex() + 1;
        String horaInicio = inicio.getText();
        String horaFin = fin.getText();
        trabajando(true);
        Interfaz.mensaje(mensaje, "Guardando horario...", false);
        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() {
                if (id == 0) {
                    controlador.registrarHorario(idOdontologo, diaElegido, horaInicio, horaFin);
                }
                else { controlador.actualizarHorario(id, idOdontologo, diaElegido, horaInicio, horaFin); }
                return null;
            }
            @Override protected void done() {
                trabajando(false);
                try {
                    get();
                    idEdicion = 0;
                    limpiarFormulario();
                    cargar(id == 0 ? "Horario guardado correctamente." : "Horario actualizado correctamente.");
                } catch (InterruptedException | ExecutionException ex) {
                    Interfaz.mensaje(mensaje, Interfaz.error(ex), true);
                }
            }
        }.execute();
    }

    private void cargar(String aviso) {
        trabajando(true);
        Interfaz.mensaje(mensaje, "Consultando horarios...", false);
        new SwingWorker<List<Horario>, Void>() {
            @Override protected List<Horario> doInBackground() { return controlador.listarHorarios(idOdontologo); }
            @Override protected void done() {
                trabajando(false);
                try {
                    List<Horario> lista = get();
                    mostrarHorarios(lista);
                    Interfaz.mensaje(mensaje, aviso.isEmpty() && lista.isEmpty() ? "Este odontólogo todavía no tiene horarios registrados." : aviso, false);
                } catch (InterruptedException | ExecutionException ex) {
                    Interfaz.mensaje(mensaje, aviso + " " + Interfaz.error(ex) + " Pulsa Actualizar listado para consultar de nuevo.", true);
                }
            }
        }.execute();
    }

    private void editar() {
        if (idEdicion > 0 || tieneDatosEnFormulario()) {
            Interfaz.mensaje(mensaje, "Guarda o limpia el formulario antes de elegir otro horario.", true);
            return;
        }
        Horario elegido = getSeleccionado();
        if (elegido == null) {
            Interfaz.mensaje(mensaje, "Selecciona un horario de la tabla para editarlo.", true);
            return;
        }
        idEdicion = elegido.getId();
        mostrarEdicion(elegido);
        Interfaz.mensaje(mensaje, "Modifica el día o las horas y pulsa Guardar cambios.", false);
    }

}
