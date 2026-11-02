package pe.oralcenter.view;

import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import pe.oralcenter.controller.*;
import pe.oralcenter.model.*;

class InicioVista extends JPanel {
    final JButton nuevaCita = new JButton("Registrar cita");
    final JButton verAgenda = new JButton("Ver agenda");
    final JButton verPacientes = new JButton("Pacientes");
    final JButton actualizar = new JButton("Actualizar resumen");
    final JLabel[] cifras = new JLabel[4];
    final JTextArea mensaje = new JTextArea(2, 1);
    private SwingWorker<List<CitaAgenda>, Void> carga;
    private final DefaultTableModel datos = new DefaultTableModel(
            new String[]{"Hora", "Paciente", "Odontólogo", "Estado"}, 0) {
        @Override public boolean isCellEditable(int fila, int columna) { return false; }
    };
    final JTable tabla = new JTable(datos);

    InicioVista() {
        super(new BorderLayout());
        JPanel principal = Interfaz.principal("Panel Principal", mensaje, actualizar);
        String[] nombres = {"Citas del día", "Pacientes", "Citas pendientes", "Citas canceladas"};
        JPanel resumen = Interfaz.panel(new GridLayout(1, 4, 16, 0));
        for (int i = 0; i < nombres.length; i++) {
            JPanel tarjeta = new JPanel(new BorderLayout(0, 10));
            tarjeta.setBackground(Color.WHITE);
            tarjeta.setBorder(new EmptyBorder(14, 20, 14, 12));
            JLabel nombre = new JLabel(nombres[i]);
            nombre.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            nombre.setForeground(new Color(0x52677F));
            cifras[i] = new JLabel("—");
            cifras[i].setFont(new Font("Segoe UI", Font.BOLD, 28));
            cifras[i].setForeground(Interfaz.AZUL);
            tarjeta.add(nombre, BorderLayout.NORTH);
            tarjeta.add(cifras[i], BorderLayout.CENTER);
            resumen.add(tarjeta);
        }
        JPanel agenda = Interfaz.tarjeta(new BorderLayout(0, 16));
        JLabel titulo = new JLabel("Agenda del día");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titulo.setForeground(Interfaz.AZUL);
        agenda.add(titulo, BorderLayout.NORTH);
        Interfaz.tabla(tabla, "Citas del día", 80, 190, 160, 120);
        JScrollPane listado = new JScrollPane(tabla);
        listado.setBorder(BorderFactory.createEmptyBorder());
        agenda.add(listado, BorderLayout.CENTER);

        JPanel accesos = Interfaz.tarjeta(new BorderLayout(0, 18));
        accesos.setPreferredSize(new Dimension(220, 0));
        JLabel tituloAccesos = new JLabel("Accesos rápidos");
        tituloAccesos.setFont(titulo.getFont());
        tituloAccesos.setForeground(Interfaz.AZUL);
        accesos.add(tituloAccesos, BorderLayout.NORTH);
        JPanel botones = Interfaz.botones(nuevaCita, verPacientes, verAgenda);
        botones.setLayout(new GridLayout(3, 1, 0, 12));
        Interfaz.primario(nuevaCita);
        JPanel superior = Interfaz.panel(new BorderLayout());
        superior.add(botones, BorderLayout.NORTH);
        accesos.add(superior, BorderLayout.CENTER);
        JPanel detalle = Interfaz.panel(new BorderLayout(20, 0));
        detalle.add(agenda, BorderLayout.CENTER);
        detalle.add(accesos, BorderLayout.EAST);
        JPanel centro = Interfaz.panel(new BorderLayout(0, 24));
        centro.add(resumen, BorderLayout.NORTH);
        centro.add(detalle, BorderLayout.CENTER);
        principal.add(centro, BorderLayout.CENTER);
        add(principal, BorderLayout.CENTER);
    }

    void cargar(AccesoControlador acceso, PacientesControlador pacientes, CitasControlador citas) {
        if (acceso == null || pacientes == null || citas == null) { return; }
        Usuario actual;
        try { actual = acceso.getUsuarioActual(); }
        catch (IllegalStateException ex) { return; }
        cancelarCarga();
        actualizar.setEnabled(false);
        carga = new SwingWorker<List<CitaAgenda>, Void>() {
            private int cantidadPacientes;
            @Override protected List<CitaAgenda> doInBackground() {
                cantidadPacientes = pacientes.buscar("").size();
                String fecha = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/uuuu"));
                return citas.consultarAgenda(fecha, null, "Todos");
            }
            @Override protected void done() {
                if (isCancelled()) { return; }
                try {
                    List<CitaAgenda> lista = get();
                    if (acceso.getUsuarioActual() != actual) { return; }
                    int pendientes = 0;
                    int canceladas = 0;
                    for (CitaAgenda fila : lista) {
                        Cita cita = fila.getCita();
                        datos.addRow(new Object[]{cita.getHora(), fila.getPaciente(), fila.getOdontologo(), cita.getEstado()});
                        if (cita.getEstado().equals("PENDIENTE")) { pendientes++; }
                        if (cita.getEstado().equals("CANCELADA")) { canceladas++; }
                    }
                    int[] cantidades = {lista.size(), cantidadPacientes, pendientes, canceladas};
                    for (int i = 0; i < cifras.length; i++) { cifras[i].setText(Integer.toString(cantidades[i])); }
                } catch (InterruptedException | ExecutionException ex) {
                    Interfaz.mensaje(mensaje, Interfaz.error(ex), true);
                } catch (IllegalStateException ex) {
                    // La sesión puede haberse cerrado mientras se consultaba el resumen.
                } finally { actualizar.setEnabled(true); }
            }
        };
        carga.execute();
    }

    void cancelarCarga() {
        if (carga != null) { carga.cancel(true); }
        datos.setRowCount(0);
        for (JLabel cifra : cifras) { cifra.setText("—"); }
        Interfaz.mensaje(mensaje, "", false);
    }
}
