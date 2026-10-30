package pe.oralcenter.view;

import java.awt.*;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.filechooser.FileNameExtensionFilter;
import pe.oralcenter.controller.CitasControlador;
import pe.oralcenter.model.*;

/** Consulta de citas; esta pantalla no modifica sus datos. */
public class AgendaVista extends JPanel {
    private CitasControlador controlador;
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/uuuu");
    private List<Odontologo> odontologos = List.of();
    private List<CitaAgenda> citas = List.of();
    final JTextField fecha = new JTextField(LocalDate.now().format(FECHA));
    final JComboBox<String> profesional = new JComboBox<>(new String[]{"Todos"});
    final JComboBox<String> estado = new JComboBox<>(new String[]{"Todos", "PENDIENTE", "CONFIRMADA", "ATENDIDA", "CANCELADA"});
    final JButton consultar = new JButton("Consultar");
    final JButton restablecer = new JButton("Restablecer");
    final JButton exportar = new JButton("Exportar Excel");
    final JButton volver = new JButton("Volver");
    final JTextArea mensaje = new JTextArea(3, 1);
    final JTextArea detalle = new JTextArea(5, 1);
    private final DefaultTableModel datos = new DefaultTableModel(
            new String[]{"Código", "Inicio", "Fin", "Paciente", "Odontólogo", "Atención", "Estado"}, 0) {
        @Override public boolean isCellEditable(int fila, int columna) { return false; }
    };
    final JTable tabla = new JTable(datos);

    public AgendaVista() {
        super(new BorderLayout());
        JPanel principal = Interfaz.principal("Agenda", mensaje, exportar, volver);
        exportar.setEnabled(false);
        JPanel filtros = Interfaz.fila(Interfaz.campo("Fecha (día/mes/año)", fecha),
                Interfaz.campo("Odontólogo", profesional), Interfaz.campo("Estado", estado));
        JPanel busqueda = Interfaz.panel(new BorderLayout(0, 12));
        busqueda.add(filtros, BorderLayout.NORTH);
        busqueda.add(Interfaz.botones(consultar, restablecer), BorderLayout.SOUTH);
        JPanel centro = Interfaz.panel(new BorderLayout(0, 16));
        centro.add(busqueda, BorderLayout.NORTH);
        Interfaz.tabla(tabla, "Citas de la agenda", 60, 65, 65, 210, 190, 140, 110);
        centro.add(new JScrollPane(tabla), BorderLayout.CENTER);
        detalle.setEditable(false);
        detalle.setLineWrap(true);
        detalle.setWrapStyleWord(true);
        detalle.setRows(4);
        detalle.getAccessibleContext().setAccessibleName("Detalle de la cita seleccionada");
        Interfaz.fuente(detalle);
        centro.add(new JScrollPane(detalle), BorderLayout.SOUTH);


        principal.add(centro, BorderLayout.CENTER);
        add(principal, BorderLayout.CENTER);
    }

    public void iniciar(CitasControlador controlador) {
        this.controlador = controlador;
        consultar.addActionListener(e -> consultar());
        exportar.addActionListener(e -> exportar());
        restablecer.addActionListener(e -> {
            fecha.setText(LocalDate.now().format(FECHA));
            profesional.setSelectedIndex(0);
            estado.setSelectedIndex(0);
            consultar();
        });
        profesional.addActionListener(e -> limpiarResultados());
        estado.addActionListener(e -> limpiarResultados());
        fecha.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { limpiarResultados(); }
            @Override public void removeUpdate(DocumentEvent e) { limpiarResultados(); }
            @Override public void changedUpdate(DocumentEvent e) { limpiarResultados(); }
        });
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) { mostrarDetalle(); }
        });
        consultar();
    }

    private void consultar() {
        String dia = fecha.getText().trim();
        String filtroEstado = (String) estado.getSelectedItem();
        Long filtroProfesional = profesionalSeleccionado();
        limpiarResultados();
        trabajando(true);
        Interfaz.mensaje(mensaje, "Consultando agenda...", false);
        new SwingWorker<List<CitaAgenda>, Void>() {
            private List<Odontologo> catalogo;
            @Override protected List<CitaAgenda> doInBackground() {
                catalogo = controlador.listarOdontologos();
                return controlador.consultarAgenda(dia, filtroProfesional, filtroEstado);
            }
            @Override protected void done() {
                try {
                    List<CitaAgenda> resultado = get();
                    odontologos = catalogo;
                    profesional.removeAllItems();
                    profesional.addItem("Todos");
                    for (int i = 0; i < catalogo.size(); i++) {
                        profesional.addItem(catalogo.get(i).toString());
                        if (filtroProfesional != null && catalogo.get(i).getId() == filtroProfesional) { profesional.setSelectedIndex(i + 1); }
                    }
                    mostrarAgenda(resultado);
                    String texto = resultado.size() + " citas para " + dia + ".";
                    if (resultado.isEmpty()) { texto = "No hay citas para la fecha y los filtros elegidos."; }
                    Interfaz.mensaje(mensaje, texto, false);
                } catch (InterruptedException | ExecutionException ex) {
                    Interfaz.mensaje(mensaje, Interfaz.error(ex) + " Pulsa Consultar para reintentar.", true);
                } finally { trabajando(false); }
            }
        }.execute();
    }

    void mostrarAgenda(List<CitaAgenda> lista) {
        limpiarResultados();
        citas = lista;
        for (CitaAgenda fila : lista) {
            Cita cita = fila.getCita();
            datos.addRow(new Object[]{cita.getId(), cita.getHora(), cita.getFin().toLocalTime(),
                    fila.getPaciente(), fila.getOdontologo(), fila.getAtencion(), cita.getEstado()});
        }
        exportar.setEnabled(!lista.isEmpty());
    }

    private void limpiarResultados() {
        tabla.clearSelection();
        citas = List.of();
        exportar.setEnabled(false);
        datos.setRowCount(0);
        detalle.setText("");
        Interfaz.mensaje(mensaje, "Pulsa Consultar para ver las citas con estos filtros.", false);
    }

    private void mostrarDetalle() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) { return; }
        CitaAgenda dato = citas.get(fila);
        Cita cita = dato.getCita();
        detalle.setText("Cita " + cita.getId() + " · " + cita.getFecha().format(FECHA) + " · " + cita.getHora() + "–" + cita.getFin().toLocalTime()
                + " · " + cita.getDuracion() + " minutos · " + cita.getEstado() + "\nPaciente: " + dato.getPaciente()
                + "\nOdontólogo: " + dato.getOdontologo() + " · Atención: " + dato.getAtencion() + "\nMotivo: " + cita.getMotivo());
        detalle.setCaretPosition(0);
    }

    private void trabajando(boolean ocupado) {
        for (JComponent campo : new JComponent[]{fecha, profesional, estado, consultar, restablecer, volver, tabla}) { campo.setEnabled(!ocupado); }
        exportar.setEnabled(!ocupado && !citas.isEmpty());
        Interfaz.navegacion(this, ocupado);
    }

    private Long profesionalSeleccionado() {
        int indice = profesional.getSelectedIndex();
        if (indice > 0) { return odontologos.get(indice - 1).getId(); }
        return null;
    }

    private void exportar() {
        Path archivo = elegirArchivo();
        if (archivo == null) { return; }
        String dia = fecha.getText().trim();
        Long filtroProfesional = profesionalSeleccionado();
        String filtroEstado = (String) estado.getSelectedItem();
        trabajando(true);
        Interfaz.mensaje(mensaje, "Consultando y exportando las citas con estos filtros...", false);
        new SwingWorker<Integer, Void>() {
            @Override protected Integer doInBackground() {
                return controlador.exportarAgenda(archivo, dia, filtroProfesional, filtroEstado);
            }
            @Override protected void done() {
                try {
                    int cantidad = get();
                    Interfaz.mensaje(mensaje, "Se exportaron " + cantidad + " citas en: " + archivo.toAbsolutePath(), false);
                } catch (InterruptedException | ExecutionException ex) {
                    Interfaz.mensaje(mensaje, Interfaz.error(ex), true);
                } finally { trabajando(false); }
            }
        }.execute();
    }

    Path elegirArchivo() {
        JFileChooser selector = new JFileChooser();
        selector.setDialogTitle("Guardar agenda en Excel");
        selector.setFileFilter(new FileNameExtensionFilter("Archivo Excel (*.xlsx)", "xlsx"));
        selector.setAcceptAllFileFilterUsed(false);
        selector.setSelectedFile(new java.io.File("agenda-" + fecha.getText().trim().replace('/', '-') + ".xlsx"));
        if (selector.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) { return null; }
        Path archivo = selector.getSelectedFile().toPath();
        if (!archivo.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".xlsx")) {
            archivo = archivo.resolveSibling(archivo.getFileName() + ".xlsx");
        }
        return archivo;
    }
}
