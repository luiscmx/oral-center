package pe.oralcenter.view;

import java.util.concurrent.ExecutionException;
import pe.oralcenter.controller.CitasControlador;

import java.awt.*;
import java.time.*;
import java.time.format.*;
import java.util.List;
import java.util.Locale;
import com.github.lgooddatepicker.components.CalendarPanel;
import com.github.lgooddatepicker.components.DatePickerSettings;
import com.github.lgooddatepicker.optionalusertools.CalendarListener;
import com.github.lgooddatepicker.zinternaltools.CalendarSelectionEvent;
import com.github.lgooddatepicker.zinternaltools.YearMonthChangeEvent;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import pe.oralcenter.model.*;

public class CitasVista extends JPanel {
    private CitasControlador controlador;

    private static final Color AZUL = new Color(0x254E88);
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);
    final SelectorPaciente paciente = new SelectorPaciente();
    final JComboBox<Odontologo> odontologo = new JComboBox<>();
    final JComboBox<TipoAtencion> tipo = new JComboBox<>();
    final JTextField fecha = new JTextField(LocalDate.now().format(FECHA));
    final JButton elegirFecha = new JButton(new ImageIcon(CalendarPanel.class.getResource("/images/datepickerbutton1.png")));
    final JPopupMenu menuCalendario = new JPopupMenu();
    CalendarPanel calendario;
    final JTextField hora = new JTextField();
    final JTextField duracion = new JTextField();
    final JLabel fin = new JLabel("Fin calculado: —");
    final JTextArea motivo = new JTextArea(2, 1);
    final JTextArea mensaje = new JTextArea(3, 1);
    final JButton consultar = new JButton("Consultar disponibilidad");
    final JButton usar = new JButton("Usar intervalo seleccionado");
    final JButton guardar = new JButton("Guardar cita");
    final JButton limpiar = new JButton("Limpiar cita");
    final JButton actualizar = new JButton("Actualizar datos");
    final JButton volver = new JButton("Volver");
    private boolean actualizandoDatos;
    private List<Intervalo> intervalos = List.of();
    private final DefaultTableModel datos = new DefaultTableModel(new String[]{"Libre desde", "Hasta"}, 0) {
        @Override public boolean isCellEditable(int fila, int columna) { return false; }
    };
    final JTable tabla = new JTable(datos);

    public CitasVista() {
        super(new BorderLayout());
        JPanel principal = Interfaz.principal("Registrar nueva cita", mensaje, volver);
        JPanel encabezado = Interfaz.panel(new BorderLayout(20, 0));
        encabezado.add(Interfaz.titulo("Registrar nueva cita"), BorderLayout.WEST);
        encabezado.add(actualizar, BorderLayout.EAST);
        Interfaz.boton(actualizar);
        principal.add(encabezado, BorderLayout.NORTH);
        JPanel centro = Interfaz.panel(new BorderLayout(20, 0));
        JPanel selectorFecha = Interfaz.panel(new BorderLayout(6, 0));
        elegirFecha.setPreferredSize(new Dimension(34, 30));
        elegirFecha.setMargin(new Insets(0, 0, 0, 0));
        elegirFecha.setToolTipText("Elegir fecha");
        elegirFecha.getAccessibleContext().setAccessibleName("Elegir fecha");
        selectorFecha.add(fecha, BorderLayout.CENTER);
        selectorFecha.add(elegirFecha, BorderLayout.EAST);

        JPanel campos = Interfaz.panel(new GridLayout(3, 1, 0, 12));
        campos.add(Interfaz.fila(Interfaz.campo("Paciente (DNI o nombre)", paciente), Interfaz.campo("Odontólogo", odontologo)));
        campos.add(Interfaz.fila(Interfaz.campo("Fecha (día/mes/año)", selectorFecha), Interfaz.campo("Tipo de atención", tipo)));
        campos.add(Interfaz.fila(Interfaz.campo("Hora de inicio (24 horas)", hora), Interfaz.campo("Duración (minutos)", duracion)));
        JPanel detalle = Interfaz.panel(new BorderLayout(0, 12));
        detalle.add(fin, BorderLayout.NORTH);
        motivo.setLineWrap(true);
        motivo.setWrapStyleWord(true);
        JScrollPane textoMotivo = new JScrollPane(motivo);
        textoMotivo.setPreferredSize(new Dimension(200, 64));
        detalle.add(Interfaz.campo("Motivo de consulta", textoMotivo), BorderLayout.CENTER);
        JPanel datosCita = Interfaz.panel(new BorderLayout(0, 18));
        datosCita.add(campos, BorderLayout.NORTH);
        datosCita.add(detalle, BorderLayout.CENTER);
        JPanel formulario = Interfaz.tarjeta(new BorderLayout(0, 18));
        formulario.add(datosCita, BorderLayout.NORTH);
        formulario.add(Interfaz.botones(guardar, limpiar), BorderLayout.SOUTH);
        centro.add(formulario, BorderLayout.CENTER);

        JPanel disponibilidad = Interfaz.tarjeta(new BorderLayout(0, 12));
        disponibilidad.setPreferredSize(new Dimension(260, 360));
        JLabel tituloDisponibilidad = new JLabel("Disponibilidad");
        tituloDisponibilidad.setFont(new Font("Segoe UI", Font.BOLD, 16));
        JPanel consulta = Interfaz.panel(new BorderLayout(0, 12));
        consulta.add(tituloDisponibilidad, BorderLayout.NORTH);
        consulta.add(Interfaz.botones(consultar), BorderLayout.CENTER);
        Interfaz.boton(usar);
        disponibilidad.add(consulta, BorderLayout.NORTH);
        Interfaz.tabla(tabla, "Intervalos disponibles");
        disponibilidad.add(new JScrollPane(tabla), BorderLayout.CENTER);
        usar.setText("Usar horario seleccionado");
        disponibilidad.add(usar, BorderLayout.SOUTH);
        centro.add(disponibilidad, BorderLayout.EAST);
        principal.add(centro, BorderLayout.CENTER);
        Interfaz.fuente(motivo, fin, actualizar, consultar, usar, fecha, elegirFecha);
        guardar.setBackground(AZUL);
        guardar.setForeground(Color.WHITE);
        Interfaz.primario(guardar);
        add(principal, BorderLayout.CENTER);


        elegirFecha.addActionListener(e -> abrirCalendario());
        tipo.addActionListener(e -> {
            if (!actualizandoDatos && tipo.getSelectedItem() != null) {
                duracion.setText(Integer.toString(((TipoAtencion) tipo.getSelectedItem()).getDuracionSugerida()));
            }
        });
        odontologo.addActionListener(e -> invalidarDisponibilidad());
        DocumentListener cambios = new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { cambiar(e); }
            @Override public void removeUpdate(DocumentEvent e) { cambiar(e); }
            @Override public void changedUpdate(DocumentEvent e) { cambiar(e); }
            private void cambiar(DocumentEvent e) {
                actualizarFin();
                if (e.getDocument() != hora.getDocument()) {
                    invalidarDisponibilidad();
                }
            }
        };
        fecha.getDocument().addDocumentListener(cambios);
        duracion.getDocument().addDocumentListener(cambios);
        hora.getDocument().addDocumentListener(cambios);
    }


    public void mostrarCatalogos(List<Paciente> pacientes, List<Odontologo> odontologos, List<TipoAtencion> tipos) {
        Odontologo anteriorOdontologo = (Odontologo) odontologo.getSelectedItem();
        TipoAtencion anteriorTipo = (TipoAtencion) tipo.getSelectedItem();
        actualizandoDatos = true;
        paciente.mostrarPacientes(pacientes);
        odontologo.removeAllItems();
        tipo.removeAllItems();
        for (Odontologo dato : odontologos) {
            odontologo.addItem(dato);
        }
        for (TipoAtencion dato : tipos) {
            tipo.addItem(dato);
        }
        if (anteriorOdontologo != null) {
            odontologo.setSelectedIndex(-1);
            for (Odontologo dato : odontologos) { if (dato.getId() == anteriorOdontologo.getId()) { odontologo.setSelectedItem(dato); } }
        }
        if (anteriorTipo != null) {
            tipo.setSelectedIndex(-1);
            for (TipoAtencion dato : tipos) { if (dato.getId() == anteriorTipo.getId()) { tipo.setSelectedItem(dato); } }
        }
        actualizandoDatos = false;
        if (anteriorTipo == null && tipo.getSelectedItem() != null && duracion.getText().isBlank()) {
            duracion.setText(Integer.toString(((TipoAtencion) tipo.getSelectedItem()).getDuracionSugerida()));
        }
        invalidarDisponibilidad();
    }

    private void abrirCalendario() {
        DatePickerSettings ajustes = new DatePickerSettings(Locale.forLanguageTag("es-PE"));
        ajustes.setVisibleClearButton(false);
        calendario = new CalendarPanel(ajustes);
        try { calendario.setSelectedDate(LocalDate.parse(fecha.getText().trim(), FECHA)); }
        catch (DateTimeException ex) { calendario.setSelectedDate(LocalDate.now()); }
        calendario.addCalendarListener(new CalendarListener() {
            @Override public void selectedDateChanged(CalendarSelectionEvent e) {
                if (e.getNewDate() != null) {
                    fecha.setText(e.getNewDate().format(FECHA));
                    menuCalendario.setVisible(false);
                }
            }
            @Override public void yearMonthChanged(YearMonthChangeEvent e) { }
        });
        menuCalendario.removeAll();
        menuCalendario.add(calendario);
        menuCalendario.show(fecha, 0, fecha.getHeight());
    }

    public void mostrarDisponibilidad(List<Intervalo> libres) {
        intervalos = libres;
        datos.setRowCount(0);
        for (Intervalo libre : libres) { datos.addRow(new Object[]{libre.getInicio().toLocalTime(), libre.getFin().toLocalTime()}); }
    }

    public Intervalo getIntervaloSeleccionado() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            return null;
        }
        return intervalos.get(fila);
    }

    public void invalidarDisponibilidad() {
        intervalos = List.of();
        datos.setRowCount(0);
    }

    public void limpiarCita() { hora.setText(""); motivo.setText(""); }

    private void actualizarFin() {
        fin.setText("Fin calculado: —");
        try {
            LocalDateTime inicio = LocalDate.parse(fecha.getText().trim(), FECHA).atTime(LocalTime.parse(hora.getText().trim()));
            int minutos = Integer.parseInt(duracion.getText().trim());
            if (minutos > 0 && minutos < 1440) {
                LocalDateTime hasta = inicio.plusMinutes(minutos);
                fin.setText("Fin calculado: " + hasta.format(FECHA) + " " + hasta.toLocalTime());
            }
        } catch (DateTimeException | NumberFormatException ex) { }
    }

    public void trabajando(boolean ocupado) {
        for (JComponent campo : new JComponent[]{odontologo, tipo, fecha, elegirFecha, hora, duracion, motivo,
                consultar, usar, guardar, limpiar, actualizar, volver, tabla}) { campo.setEnabled(!ocupado); }
        paciente.habilitar(!ocupado);
        if (ocupado) { menuCalendario.setVisible(false); }
        Interfaz.navegacion(this, ocupado);
    }


    // Acciones de los botones de esta ventana.
    public void iniciar(CitasControlador controlador) {
        this.controlador = controlador;
        consultar.addActionListener(e -> consultar(""));
        guardar.addActionListener(e -> guardar());
        usar.addActionListener(e -> usarIntervalo());
        actualizar.addActionListener(e -> cargarDatos());
        limpiar.addActionListener(e -> { limpiarCita(); Interfaz.mensaje(mensaje, "", false); });
        cargarDatos();
    }

    private void cargarDatos() {
        trabajando(true);
        Interfaz.mensaje(mensaje, "Consultando pacientes, odontólogos y tipos de atención...", false);
        new SwingWorker<Void, Void>() {
            private List<Paciente> pacientes;
            private List<Odontologo> odontologos;
            private List<TipoAtencion> tipos;
            @Override protected Void doInBackground() {
                pacientes = controlador.listarPacientes();
                odontologos = controlador.listarOdontologos();
                tipos = controlador.listarTiposAtencion();
                return null;
            }
            @Override protected void done() {
                trabajando(false);
                try {
                    get();
                    mostrarCatalogos(pacientes, odontologos, tipos);
                    String aviso = "";
                    if (pacientes.isEmpty()) {
                        aviso = "No hay pacientes disponibles para tu perfil. Recepción puede registrar al paciente y su primera cita.";
                    }
                    if (odontologos.isEmpty()) {
                        aviso = "No hay odontólogos disponibles. Revisa su registro con administración.";
                    }
                    if (tipos.isEmpty()) {
                        aviso = "No hay tipos de atención registrados. Revisa el catálogo con administración.";
                    }
                    Interfaz.mensaje(mensaje, aviso, false);
                } catch (InterruptedException | ExecutionException ex) {
                    Interfaz.mensaje(mensaje, Interfaz.error(ex) + " Pulsa Actualizar datos para reintentar.", true);
                }
            }
        }.execute();
    }

    private void consultar(String aviso) {
        Odontologo profesional = (Odontologo) odontologo.getSelectedItem();
        long id = profesional == null ? 0 : profesional.getId();
        String fechaIngresada = fecha.getText();
        String duracionIngresada = duracion.getText();
        trabajando(true);
        Interfaz.mensaje(mensaje, aviso + " Consultando disponibilidad...", false);
        invalidarDisponibilidad();
        new SwingWorker<List<Intervalo>, Void>() {
            @Override protected List<Intervalo> doInBackground() { return controlador.consultarDisponibilidad(id, fechaIngresada, duracionIngresada); }
            @Override protected void done() {
                trabajando(false);
                try {
                    List<Intervalo> libres = get();
                    mostrarDisponibilidad(libres);
                    String texto = "Selecciona un intervalo y pulsa Usar horario seleccionado, o escribe otra hora dentro de él. Se comprobará de nuevo al guardar.";
                    if (libres.isEmpty()) {
                        texto = "No hay intervalos con tiempo suficiente. Prueba otra fecha o revisa los turnos con administración.";
                    }
                    Interfaz.mensaje(mensaje, aviso + " " + texto, false);
                } catch (InterruptedException | ExecutionException ex) {
                    Interfaz.mensaje(mensaje, aviso + " " + Interfaz.error(ex), true);
                }
            }
        }.execute();
    }

    private void usarIntervalo() {
        Intervalo elegido = getIntervaloSeleccionado();
        if (elegido == null) {
            Interfaz.mensaje(mensaje, "Selecciona un intervalo de la tabla.", true);
            return;
        }
        hora.setText(elegido.getInicio().toLocalTime().toString());
        Interfaz.mensaje(mensaje, "Revisa la hora de fin y completa el motivo antes de guardar.", false);
    }

    private void guardar() {
        Paciente persona = paciente.getSeleccionado();
        Odontologo profesional = (Odontologo) odontologo.getSelectedItem();
        TipoAtencion atencion = (TipoAtencion) tipo.getSelectedItem();
        String fechaIngresada = fecha.getText();
        String horaIngresada = hora.getText();
        String duracionIngresada = duracion.getText();
        String motivoIngresado = motivo.getText();
        trabajando(true);
        Interfaz.mensaje(mensaje, "Comprobando y guardando la cita...", false);
        new SwingWorker<Long, Void>() {
            @Override protected Long doInBackground() {
                long idPaciente = 0;
                long idOdontologo = 0;
                long idTipo = 0;
                if (persona != null) {
                    idPaciente = persona.getId();
                }
                if (profesional != null) {
                    idOdontologo = profesional.getId();
                }
                if (atencion != null) {
                    idTipo = atencion.getId();
                }
                return controlador.registrar(idPaciente, idOdontologo, idTipo, fechaIngresada, horaIngresada, duracionIngresada, motivoIngresado);
            }
            @Override protected void done() {
                trabajando(false);
                try {
                    long id = get();
                    limpiarCita();
                    consultar("Cita " + id + " guardada como pendiente para " + fechaIngresada + " a las " + horaIngresada + ".");
                } catch (InterruptedException | ExecutionException ex) {
                    invalidarDisponibilidad();
                    Interfaz.mensaje(mensaje, Interfaz.error(ex), true);
                }
            }
        }.execute();
    }

}
