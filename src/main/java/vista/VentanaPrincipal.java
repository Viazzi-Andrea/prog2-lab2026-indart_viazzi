package vista;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;

import modelo.Registro;

public class VentanaPrincipal extends JFrame {
    private final JTextField campoIdentificador = new JTextField(10);
    private final JTextField campoNombre = new JTextField(20);
    private final JComboBox<String> comboCategoria =
            new JComboBox<>(new String[] {"Notebook", "Proyector", "Kit de robótica", "Aula"});
    private final JCheckBox casillaActivo = new JCheckBox("", true);
    private final JTextField campoBusqueda = new JTextField(20);
    private final JRadioButton radioDisponible = new JRadioButton("Disponible", true);
    private final JRadioButton radioPrestado = new JRadioButton("Prestado");
    private final JRadioButton radioReparacion = new JRadioButton("En reparación");
    private final ButtonGroup grupoEstado = new ButtonGroup();
    private final JSpinner spinnerDias = new JSpinner(new SpinnerNumberModel(1, 1, 15, 1));
    private final JTextArea areaObservaciones = new JTextArea(3, 20);
    private final JButton botonRegistrar = new JButton("Registrar");
    private final JButton botonModificar = new JButton("Modificar");
    private final JButton botonEliminar = new JButton("Eliminar");
    private final JButton botonBuscar = new JButton("Buscar");
    private final JButton botonLimpiar = new JButton("Limpiar");
    private final JButton botonGuardar = new JButton("Guardar");
    private final DefaultTableModel modeloTabla = new DefaultTableModel(
            new String[] {"Identificador", "Nombre", "Categoria", "Activo"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };
    private final JTable tablaRegistros = new JTable(modeloTabla);

    public VentanaPrincipal() {
        super("Gestor de registros - Prototipo");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        grupoEstado.add(radioDisponible);
        grupoEstado.add(radioPrestado);
        grupoEstado.add(radioReparacion);

        JPanel panelEstado = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        panelEstado.add(radioDisponible);
        panelEstado.add(radioPrestado);
        panelEstado.add(radioReparacion);

        areaObservaciones.setLineWrap(true);
        areaObservaciones.setWrapStyleWord(true);

                JPanel formulario = new JPanel(new GridBagLayout());
                formulario.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        GridBagConstraints etiqueta = new GridBagConstraints();
        etiqueta.gridx = 0;
        etiqueta.anchor = GridBagConstraints.WEST;
        etiqueta.insets = new Insets(6, 6, 6, 12);

        GridBagConstraints campoAncho = new GridBagConstraints();
        campoAncho.gridx = 1;
        campoAncho.fill = GridBagConstraints.HORIZONTAL;
        campoAncho.weightx = 1;
        campoAncho.insets = new Insets(6, 0, 6, 6);

        GridBagConstraints campoCorto = new GridBagConstraints();
        campoCorto.gridx = 1;
        campoCorto.anchor = GridBagConstraints.WEST;
        campoCorto.insets = new Insets(6, 0, 6, 6);

        etiqueta.gridy = 0;
        campoAncho.gridy = 0;
        formulario.add(new JLabel("Identificador:"), etiqueta);
        formulario.add(campoIdentificador, campoAncho);

        etiqueta.gridy = 1;
        campoAncho.gridy = 1;
        formulario.add(new JLabel("Nombre:"), etiqueta);
        formulario.add(campoNombre, campoAncho);

        etiqueta.gridy = 2;
        campoAncho.gridy = 2;
        formulario.add(new JLabel("Categoría:"), etiqueta);
        formulario.add(comboCategoria, campoAncho);

        etiqueta.gridy = 3;
        campoCorto.gridy = 3;
        formulario.add(new JLabel("Activo:"), etiqueta);
        formulario.add(casillaActivo, campoCorto);

        etiqueta.gridy = 4;
        campoCorto.gridy = 4;
        formulario.add(new JLabel("Estado:"), etiqueta);
        formulario.add(panelEstado, campoCorto);

        etiqueta.gridy = 5;
        campoCorto.gridy = 5;
        formulario.add(new JLabel("Días de préstamo:"), etiqueta);
        formulario.add(spinnerDias, campoCorto);

        etiqueta.gridy = 6;
        campoAncho.gridy = 6;
        formulario.add(new JLabel("Observaciones:"), etiqueta);
        formulario.add(new JScrollPane(areaObservaciones), campoAncho);

        JPanel acciones = new JPanel(new FlowLayout());
        acciones.add(botonRegistrar);
        acciones.add(botonModificar);
        acciones.add(botonEliminar);
        acciones.add(botonLimpiar);
        acciones.add(botonGuardar);

        JPanel panelSuperior = new JPanel(new BorderLayout());
        panelSuperior.add(formulario, BorderLayout.CENTER);
        panelSuperior.add(acciones, BorderLayout.SOUTH);

        JPanel busqueda = new JPanel(new FlowLayout());
        busqueda.add(new JLabel("Buscar:"));
        busqueda.add(campoBusqueda);
        busqueda.add(botonBuscar);

        add(panelSuperior, BorderLayout.NORTH);
        add(new JScrollPane(tablaRegistros), BorderLayout.CENTER);
        add(busqueda, BorderLayout.SOUTH);
        tablaRegistros.setRowHeight(22);
        tablaRegistros.setPreferredScrollableViewportSize(new Dimension(880, 240));
        pack();
        setMinimumSize(getSize());
        setLocationRelativeTo(null);
    }

    public void mostrarRegistros(List<Registro> registros) {
        modeloTabla.setRowCount(0);
        for (Registro registro : registros) {
            modeloTabla.addRow(new Object[] {
                registro.getIdentificador(),
                registro.getNombre(),
                registro.getCategoria(),
                registro.isActivo() ? "Si" : "No"
            });
        }
    }

    public void mostrarMensaje(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Prototipo", JOptionPane.INFORMATION_MESSAGE);
    }

    public void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public JTextField getCampoIdentificador() {
        return campoIdentificador;
    }

    public JTextField getCampoNombre() {
        return campoNombre;
    }

    public JComboBox<String> getComboCategoria() {
        return comboCategoria;
    }

    public JCheckBox getCasillaActivo() {
        return casillaActivo;
    }

    public JTextField getCampoBusqueda() {
        return campoBusqueda;
    }

    public JTable getTablaRegistros() {
        return tablaRegistros;
    }

    public JButton getBotonRegistrar() {
        return botonRegistrar;
    }

    public JButton getBotonModificar() {
        return botonModificar;
    }

    public JButton getBotonEliminar() {
        return botonEliminar;
    }

    public JButton getBotonBuscar() {
        return botonBuscar;
    }

    public JButton getBotonLimpiar() {
        return botonLimpiar;
    }

    public JButton getBotonGuardar() {
        return botonGuardar;
    }
}
