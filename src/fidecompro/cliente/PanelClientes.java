package fidecompro.cliente;

import fidecompro.comun.Cliente;
import fidecompro.excepciones.DatoInvalidoException;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

/** Creacion, modificacion y consulta de clientes. */
public class PanelClientes extends JPanel implements Refrescable {

    private final JTextField txtCedula = new JTextField(14);
    private final JTextField txtNombre = new JTextField(20);
    private final JTextField txtTelefono = new JTextField(14);
    private final JTextField txtCorreo = new JTextField(20);
    private final JTextField txtDireccion = new JTextField(40);

    private final DefaultTableModel modelo = new DefaultTableModel(
            new String[]{"ID", "Cédula", "Nombre", "Teléfono", "Correo", "Dirección"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int col) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modelo);
    private ArrayList<Cliente> datos = new ArrayList<>();
    private Cliente seleccionado;

    public PanelClientes() {
        super(new BorderLayout(6, 6));

        JPanel form = new JPanel(new GridBagLayout());
        campo(form, "Cédula:", txtCedula, 0, 0, 1);
        campo(form, "Nombre:", txtNombre, 0, 1, 1);
        campo(form, "Teléfono:", txtTelefono, 1, 0, 1);
        campo(form, "Correo:", txtCorreo, 1, 1, 1);
        campo(form, "Dirección:", txtDireccion, 2, 0, 3);

        JButton btnGuardar = new JButton("Guardar nuevo");
        JButton btnActualizar = new JButton("Actualizar");
        JButton btnLimpiar = new JButton("Limpiar");
        JButton btnRefrescar = new JButton("Refrescar");
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
        botones.add(btnGuardar);
        botones.add(btnActualizar);
        botones.add(btnLimpiar);
        botones.add(btnRefrescar);

        JPanel norte = new JPanel(new BorderLayout());
        norte.add(form, BorderLayout.CENTER);
        norte.add(botones, BorderLayout.SOUTH);
        add(norte, BorderLayout.NORTH);

        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                cargarSeleccion();
            }
        });
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        btnGuardar.addActionListener(e -> guardarNuevo());
        btnActualizar.addActionListener(e -> actualizar());
        btnLimpiar.addActionListener(e -> limpiar());
        btnRefrescar.addActionListener(e -> refrescar());
    }

    private static void campo(JPanel p, String texto, JComponent c, int fila, int col, int ancho) {
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 6, 4, 6);
        g.gridy = fila;
        g.anchor = GridBagConstraints.WEST;
        g.gridx = col * 2;
        p.add(new JLabel(texto), g);
        g.gridx = col * 2 + 1;
        g.gridwidth = ancho;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;
        p.add(c, g);
    }

    /** Crea el objeto cliente con los datos del formulario y lo valida*/
    private Cliente leerFormulario(int id) {
        Cliente c = new Cliente(id, txtCedula.getText().trim(), txtNombre.getText().trim(),
                txtTelefono.getText().trim(), txtCorreo.getText().trim(), txtDireccion.getText().trim());
        try {
            c.validar();
            return c;
        } catch (DatoInvalidoException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Datos incompletos", JOptionPane.WARNING_MESSAGE);
            return null;
        }
    }

    private void guardarNuevo() {
        Cliente c = leerFormulario(0);
        if (c == null) {
            return;
        }
        Tarea.ejecutar("CLIENTE_CREAR", c, r -> {
            mostrar(r.isExito(), r.getMensaje());
            if (r.isExito()) {
                limpiar();
                refrescar();
            }
        });
    }

    private void actualizar() {
        if (seleccionado == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un cliente de la tabla.",
                    "Actualizar", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Cliente c = leerFormulario(seleccionado.getId());
        if (c == null) {
            return;
        }
        Tarea.ejecutar("CLIENTE_ACTUALIZAR", c, r -> {
            mostrar(r.isExito(), r.getMensaje());
            if (r.isExito()) {
                refrescar();
            }
        });
    }

    private void limpiar() {
        seleccionado = null;
        tabla.clearSelection();
        txtCedula.setText("");
        txtNombre.setText("");
        txtTelefono.setText("");
        txtCorreo.setText("");
        txtDireccion.setText("");
    }

    private void cargarSeleccion() {
        int fila = tabla.getSelectedRow();
        if (fila < 0 || fila >= datos.size()) {
            return;
        }
        seleccionado = datos.get(fila);
        txtCedula.setText(seleccionado.getCedula());
        txtNombre.setText(seleccionado.getNombre());
        txtTelefono.setText(seleccionado.getTelefono());
        txtCorreo.setText(seleccionado.getCorreo());
        txtDireccion.setText(seleccionado.getDireccion());
    }

    private void mostrar(boolean exito, String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Clientes",
                exito ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void refrescar() {
        Tarea.ejecutar("CLIENTE_LISTAR", null, r -> {
            if (!r.isExito()) {
                mostrar(false, r.getMensaje());
                return;
            }
            datos = (ArrayList<Cliente>) r.getDatos();
            modelo.setRowCount(0);
            for (Cliente c : datos) {
                modelo.addRow(new Object[]{c.getId(), c.getCedula(), c.getNombre(),
                    c.getTelefono(), c.getCorreo(), c.getDireccion()});
            }
            seleccionado = null;
        });
    }
}
