package pe.oralcenter.view;

import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.HierarchyEvent;
import java.util.List;
import java.util.Locale;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicArrowButton;
import org.apache.commons.lang3.StringUtils;
import pe.oralcenter.model.Paciente;

/** Busca entre los pacientes autorizados y permite escoger una coincidencia. */
class SelectorPaciente extends JPanel {
    final JTextField busqueda = new JTextField();
    final JButton desplegar = new BasicArrowButton(SwingConstants.SOUTH);
    final DefaultListModel<Paciente> resultados = new DefaultListModel<>();
    final JList<Paciente> lista = new JList<>(resultados);
    final JPopupMenu sugerencias = new JPopupMenu();
    final JLabel estado = new JLabel();
    final JScrollPane desplazamiento = new JScrollPane(lista);
    private List<Paciente> pacientes = List.of();
    private Paciente seleccionado;
    private boolean cambiandoTexto;

    SelectorPaciente() {
        super(new BorderLayout());
        setOpaque(false);
        add(busqueda, BorderLayout.CENTER);
        add(desplegar, BorderLayout.EAST);
        busqueda.setToolTipText("Escribe parte del DNI, nombres o apellidos y elige un paciente.");
        desplegar.setToolTipText("Mostrar pacientes");
        desplegar.setFocusable(false);
        Interfaz.fuente(busqueda, lista, estado);
        lista.setFixedCellHeight(32);
        lista.setFocusable(false);
        lista.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lista.getAccessibleContext().setAccessibleName("Pacientes encontrados");
        DefaultListCellRenderer celdas = new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> lista, Object dato, int indice, boolean elegido, boolean foco) {
                super.getListCellRendererComponent(lista, dato, indice, elegido, foco);
                if (dato instanceof Paciente) { setText(describir((Paciente) dato)); }
                return this;
            }
        };
        celdas.putClientProperty("html.disable", true);
        lista.setCellRenderer(celdas);
        sugerencias.setFocusable(false);
        sugerencias.setLayout(new BorderLayout());
        sugerencias.add(desplazamiento, BorderLayout.CENTER);
        sugerencias.add(estado, BorderLayout.SOUTH);
        busqueda.getDocument().addDocumentListener(new DocumentListener() {
            private void cambiar() {
                if (cambiandoTexto) { return; }
                seleccionado = null;
                filtrar();
                if (busqueda.isFocusOwner()) { mostrarSugerencias(); }
            }
            @Override public void insertUpdate(DocumentEvent e) { cambiar(); }
            @Override public void removeUpdate(DocumentEvent e) { cambiar(); }
            @Override public void changedUpdate(DocumentEvent e) { cambiar(); }
        });
        lista.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && lista.getSelectedValue() != null) {
                seleccionado = lista.getSelectedValue();
                escribir(describir(seleccionado));
                sugerencias.setVisible(false);
            }
        });
        desplegar.addActionListener(e -> { busqueda.requestFocusInWindow(); filtrar(); mostrarSugerencias(); });
        busqueda.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                if (seleccionado != null) { busqueda.selectAll(); }
                else { filtrar(); mostrarSugerencias(); }
            }
        });
        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && !isShowing()) { sugerencias.setVisible(false); }
        });
    }

    void mostrarPacientes(List<Paciente> nuevos) {
        pacientes = nuevos;
        if (seleccionado != null) {
            long id = seleccionado.getId();
            seleccionado = null;
            for (Paciente paciente : nuevos) { if (paciente.getId() == id) { seleccionado = paciente; } }
            escribir(seleccionado == null ? "" : describir(seleccionado));
        }
        filtrar();
    }

    private void filtrar() {
        String filtro = seleccionado == null ? normalizar(busqueda.getText()) : "";
        resultados.clear();
        for (Paciente paciente : pacientes) {
            String texto = normalizar(paciente.getDni() + " " + paciente.getNombres() + " " + paciente.getApellidos());
            boolean coincide = true;
            if (filtro.matches("[0-9]+")) { coincide = paciente.getDni().startsWith(filtro); }
            else {
                for (String palabra : filtro.split("\\s+")) {
                    if (!texto.contains(palabra)) { coincide = false; break; }
                }
            }
            if (coincide) { resultados.addElement(paciente); }
        }
        if (resultados.isEmpty()) { estado.setText("Sin coincidencias. Prueba con otro dato."); }
        else if (resultados.size() == 1) { estado.setText("1 encontrado. Elige un paciente."); }
        else { estado.setText(resultados.size() + " encontrados. Elige un paciente."); }
    }

    private void mostrarSugerencias() {
        if (!isShowing() || !busqueda.isEnabled()) { return; }
        int alto = Math.max(1, Math.min(6, resultados.size())) * 32 + 28;
        sugerencias.setPopupSize(getWidth(), alto);
        if (!sugerencias.isVisible()) { sugerencias.show(this, 0, getHeight()); }
    }

    private void escribir(String texto) {
        cambiandoTexto = true;
        busqueda.setText(texto);
        cambiandoTexto = false;
    }

    private static String describir(Paciente paciente) {
        return paciente.getDni() + " · " + paciente.getNombres() + " " + paciente.getApellidos();
    }

    private static String normalizar(String texto) {
        return StringUtils.stripAccents(texto).toLowerCase(Locale.ROOT).replaceAll("[^\\p{L}\\p{N}]+", " ").trim();
    }

    Paciente getSeleccionado() { return seleccionado; }

    void habilitar(boolean habilitado) {
        setEnabled(habilitado);
        busqueda.setEnabled(habilitado);
        desplegar.setEnabled(habilitado);
        if (!habilitado) { sugerencias.setVisible(false); }
    }
}
