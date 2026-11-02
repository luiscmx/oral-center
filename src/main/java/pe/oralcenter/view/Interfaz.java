package pe.oralcenter.view;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;

/** Componentes que se repiten en los formularios. */
class Interfaz {
    static final Color AZUL = new Color(0x254E88);
    static final Color FONDO = new Color(0xF1F7FE);

    static {
        UIManager.put("Button.gradient", null);
        UIManager.put("ComboBox.gradient", null);
    }

    static JPanel panel(LayoutManager distribucion) {
        JPanel panel = new JPanel(distribucion);
        panel.setOpaque(false);
        return panel;
    }

    static JPanel tarjeta(LayoutManager distribucion) {
        JPanel tarjeta = new JPanel(distribucion);
        tarjeta.setBackground(Color.WHITE);
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xE0E8F2)),
                new EmptyBorder(16, 16, 16, 16)));
        return tarjeta;
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
        mensaje.setVisible(false);
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
            boton(boton);
            panel.add(boton);
        }
        return panel;
    }

    static void boton(JButton boton) {
        fuente(boton);
        boton.setBackground(new Color(0xE7F2FF));
        boton.setForeground(AZUL);
        boton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xD5E4F5)),
                new EmptyBorder(8, 12, 8, 12)));
    }

    static void primario(JButton boton) {
        boton.setBackground(new Color(0x3177DC));
        boton.setForeground(Color.WHITE);
    }

    static JPanel campo(String texto, JComponent entrada) {
        JPanel campo = panel(new BorderLayout(0, 6));
        JLabel etiqueta = new JLabel(texto);
        etiqueta.setLabelFor(entrada);
        fuente(etiqueta, entrada);
        etiqueta.setForeground(new Color(0x415670));
        etiqueta.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        entrada.setPreferredSize(new Dimension(180, Math.max(34, entrada.getPreferredSize().height)));
        if (entrada instanceof JTextField || entrada instanceof JComboBox) {
            entrada.setBackground(new Color(0xF1F7FE));
            entrada.setBorder(BorderFactory.createLineBorder(new Color(0xDCE7F5)));
            if (entrada instanceof JTextField) {
                entrada.setBorder(BorderFactory.createCompoundBorder(entrada.getBorder(), new EmptyBorder(4, 7, 4, 7)));
            }
        }
        if (entrada instanceof JPanel) {
            for (Component hijo : entrada.getComponents()) {
                if (hijo instanceof JTextField) {
                    hijo.setBackground(new Color(0xF1F7FE));
                    ((JTextField) hijo).setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(new Color(0xDCE7F5)), new EmptyBorder(4, 7, 4, 7)));
                }
            }
        }
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
        tabla.setRowHeight(38);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setFillsViewportHeight(true);
        tabla.setShowGrid(false);
        tabla.setIntercellSpacing(new Dimension(0, 0));
        tabla.setSelectionBackground(new Color(0xDCE8F7));
        tabla.setSelectionForeground(new Color(0x202020));
        tabla.getTableHeader().setReorderingAllowed(false);
        tabla.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        tabla.getTableHeader().setPreferredSize(new Dimension(100, 34));
        DefaultTableCellRenderer cabecera = new DefaultTableCellRenderer();
        cabecera.setBackground(new Color(0xEDF5FF));
        cabecera.setForeground(AZUL);
        cabecera.setFont(new Font("Segoe UI", Font.BOLD, 13));
        cabecera.setBorder(new EmptyBorder(8, 8, 8, 8));
        tabla.getTableHeader().setDefaultRenderer(cabecera);
        tabla.getAccessibleContext().setAccessibleName(nombre);
        DefaultTableCellRenderer celdas = new DefaultTableCellRenderer();
        celdas.putClientProperty("html.disable", true);
        celdas.setBorder(new EmptyBorder(0, 8, 0, 8));
        tabla.setDefaultRenderer(Object.class, celdas);
        for (int i = 0; i < anchos.length; i++) {
            tabla.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
        }
        for (int i = 0; i < tabla.getColumnCount(); i++) {
            if (tabla.getColumnName(i).equals("Estado")) {
                tabla.getColumnModel().getColumn(i).setCellRenderer(new DefaultTableCellRenderer() {
                    @Override public Component getTableCellRendererComponent(JTable tabla, Object valor,
                            boolean seleccionado, boolean foco, int fila, int columna) {
                        super.getTableCellRendererComponent(tabla, valor, seleccionado, foco, fila, columna);
                        String estado = String.valueOf(valor);
                        setHorizontalAlignment(SwingConstants.CENTER);
                        setForeground(new Color(0x24578A));
                        setBackground(new Color(0xE6F1FF));
                        if (estado.equals("CONFIRMADA")) { setForeground(new Color(0x177458)); setBackground(new Color(0xE3F5ED)); }
                        if (estado.equals("PENDIENTE")) { setForeground(new Color(0x8B5C10)); setBackground(new Color(0xFFF3D4)); }
                        if (estado.equals("CANCELADA")) { setForeground(new Color(0xA4333D)); setBackground(new Color(0xFCECEF)); }
                        setBorder(BorderFactory.createCompoundBorder(
                                BorderFactory.createLineBorder(Color.WHITE, 6), new EmptyBorder(0, 4, 0, 4)));
                        putClientProperty("html.disable", true);
                        return this;
                    }
                });
            }
        }
    }

    static void fuente(JComponent... componentes) {
        for (JComponent componente : componentes) {
            componente.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        }
    }

    static void mensaje(JTextArea campo, String texto, boolean error) {
        campo.setText(texto);
        campo.setVisible(!texto.isBlank());
        campo.setForeground(error ? new Color(0xA12622) : AZUL);
        campo.setBackground(error ? new Color(0xFFF0F1) : FONDO);
        campo.setBorder(new EmptyBorder(7, 10, 7, 10));
        if (!error && (texto.contains("correctamente") || texto.startsWith("Cita registrada") || texto.startsWith("Se exportaron"))) {
            campo.setBackground(new Color(0xE6F5EE));
            campo.setForeground(new Color(0x177458));
        }
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
