package pe.oralcenter.view;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;

/** Componentes que se repiten en los formularios. */
class Interfaz {
    private static final Color AZUL = new Color(0x254E88);
    private static final Color FONDO = new Color(0xF5F7FA);

    static JPanel panel(LayoutManager distribucion) {
        JPanel panel = new JPanel(distribucion);
        panel.setOpaque(false);
        return panel;
    }

    static JLabel titulo(String texto) {
        JLabel titulo = new JLabel(texto);
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titulo.setForeground(AZUL);
        return titulo;
    }

    static JPanel principal(String titulo, JTextArea mensaje, JButton... acciones) {
        JPanel principal = panel(new BorderLayout(16, 18));
        principal.setOpaque(true);
        principal.setBackground(FONDO);
        principal.setBorder(new EmptyBorder(20, 24, 20, 24));
        principal.add(titulo(titulo), BorderLayout.NORTH);
        mensaje.setEditable(false);
        mensaje.setLineWrap(true);
        mensaje.setWrapStyleWord(true);
        mensaje.setBackground(FONDO);
        mensaje.getAccessibleContext().setAccessibleName("Resultado de la operación");
        JPanel pie = panel(new BorderLayout(16, 0));
        mensaje.setRows(2);
        pie.add(mensaje, BorderLayout.CENTER);
        pie.add(botones(acciones), BorderLayout.EAST);
        principal.add(pie, BorderLayout.SOUTH);
        fuente(mensaje);
        return principal;
    }

    static JPanel campos(String[] etiquetas, JComponent... entradas) {
        JPanel campos = panel(new GridLayout(etiquetas.length * 2, 1, 0, 6));
        for (int i = 0; i < etiquetas.length; i++) {
            JLabel etiqueta = new JLabel(etiquetas[i]);
            etiqueta.setLabelFor(entradas[i]);
            fuente(etiqueta, entradas[i]);
            campos.add(etiqueta);
            campos.add(entradas[i]);
        }
        return campos;
    }

    static JPanel botones(JButton... botones) {
        JPanel panel = panel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        for (JButton boton : botones) {
            fuente(boton);
            boton.setMargin(new Insets(6, 12, 6, 12));
            panel.add(boton);
        }
        return panel;
    }

    static JPanel campo(String texto, JComponent entrada) {
        JPanel campo = panel(new BorderLayout(0, 6));
        JLabel etiqueta = new JLabel(texto);
        etiqueta.setLabelFor(entrada);
        fuente(etiqueta, entrada);
        entrada.setPreferredSize(new Dimension(180, Math.max(30, entrada.getPreferredSize().height)));
        campo.add(etiqueta, BorderLayout.NORTH);
        campo.add(entrada, BorderLayout.CENTER);
        return campo;
    }

    static JPanel fila(JComponent... campos) {
        JPanel fila = panel(new GridLayout(1, campos.length, 16, 0));
        for (JComponent campo : campos) { fila.add(campo); }
        return fila;
    }

    static JPanel formulario(JLabel titulo, JPanel campos, int alto, JButton guardar, JButton limpiar) {
        JPanel ficha = panel(new BorderLayout(0, 16));
        ficha.setPreferredSize(new Dimension(280, alto));
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        ficha.add(titulo, BorderLayout.NORTH);
        ficha.add(campos, BorderLayout.CENTER);
        ficha.add(botones(guardar, limpiar), BorderLayout.SOUTH);
        JPanel columna = panel(new BorderLayout());
        columna.add(ficha, BorderLayout.NORTH);
        guardar.setBackground(AZUL);
        guardar.setForeground(Color.WHITE);
        return columna;
    }

    static void tabla(JTable tabla, String nombre, int... anchos) {
        fuente(tabla);
        tabla.setRowHeight(30);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setFillsViewportHeight(true);
        tabla.setShowGrid(false);
        tabla.setIntercellSpacing(new Dimension(0, 0));
        tabla.setSelectionBackground(new Color(0xDCE8F7));
        tabla.setSelectionForeground(new Color(0x202020));
        tabla.getTableHeader().setReorderingAllowed(false);
        tabla.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        tabla.getAccessibleContext().setAccessibleName(nombre);
        DefaultTableCellRenderer celdas = new DefaultTableCellRenderer();
        celdas.putClientProperty("html.disable", true);
        tabla.setDefaultRenderer(Object.class, celdas);
        for (int i = 0; i < anchos.length; i++) {
            tabla.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
        }
    }

    static void fuente(JComponent... componentes) {
        for (JComponent componente : componentes) {
            componente.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        }
    }

    static void mensaje(JTextArea campo, String texto, boolean error) {
        campo.setText(texto);
        campo.setForeground(error ? new Color(0xA12622) : AZUL);
        if (error) {
            campo.requestFocusInWindow();
        }
    }

    static String error(Exception error) {
        if (error instanceof InterruptedException) {
            Thread.currentThread().interrupt();
        }
        Throwable causa = error.getCause();
        if (causa instanceof IllegalArgumentException || causa instanceof IllegalStateException) {
            return causa.getMessage();
        }
        return "No se pudo completar la operación. Inténtalo nuevamente.";
    }

    static void navegacion(JComponent panel, boolean ocupado) {
        Window ventana = SwingUtilities.getWindowAncestor(panel);
        if (ventana instanceof AccesoVista) {
            ((AccesoVista) ventana).navegacionDisponible(!ocupado);
        }
    }
}
