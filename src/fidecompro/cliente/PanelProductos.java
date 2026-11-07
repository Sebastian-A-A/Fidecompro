package fidecompro.cliente;

import fidecompro.comun.Producto;
import fidecompro.comun.ProductoCanastaBasica;
import fidecompro.comun.ProductoElectrodomestico;
import fidecompro.comun.ProductoGeneral;
import fidecompro.comun.ProductoPerecedero;
import fidecompro.comun.Usuario;
import fidecompro.excepciones.DatoInvalidoException;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Locale;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

/**
 * Registro y consulta de productos
 */
public class PanelProductos extends JPanel implements Refrescable {

    private static final String[] TIPOS = {"Canasta básica", "General", "Perecedero", "Electrodoméstico"};

    private final JTextField txtNombre = new JTextField(20);
    private final JComboBox<String> cmbCategoria = new JComboBox<>(new String[]{
        "Abarrotes", "Bebidas", "Lácteos", "Carnes", "Limpieza", "Electrodomésticos", "Otros"});
    private final JComboBox<String> cmbTipo = new JComboBox<>(TIPOS);
    private final JTextField txtPrecio = new JTextField(10);
    private final JTextField txtStock = new JTextField(10);
    private final JLabel lblExtra = new JLabel("Dato adicional:");
    private final JTextField txtExtra = new JTextField(14);

    private final DefaultTableModel modelo = new DefaultTableModel(
            new String[]{"Código", "Nombre", "Categoría", "Tipo", "Precio", "IVA", "Stock", "Detalle"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int col) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modelo);
    private ArrayList<Producto> datos = new ArrayList<>();
    private Producto seleccionado;

    private final JButton btnGuardar = new JButton("Guardar nuevo");
    private final JButton btnActualizar = new JButton("Actualizar");
    private final JButton btnLimpiar = new JButton("Limpiar");
    private final JButton btnRefrescar = new JButton("Refrescar");

    public PanelProductos(Usuario usuario) {
        super(new BorderLayout(6, 6));

        JPanel form = new JPanel(new GridBagLayout());
        campo(form, "Nombre:", txtNombre, 0, 0);
        cmbCategoria.setEditable(true);
        campo(form, "Categoría:", cmbCategoria, 0, 1);
        campo(form, "Tipo:", cmbTipo, 1, 0);
        campo(form, "Precio:", txtPrecio, 1, 1);
        campo(form, "Stock:", txtStock, 2, 0);
        campo(form, lblExtra, txtExtra, 2, 1);

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
        int[] anchos = {60, 170, 150, 130, 80, 50, 60, 170}; // Código, Nombre, Categoría, Tipo, Precio, IVA, Stock, Detalle
        for (int i = 0; i < anchos.length; i++) {
            tabla.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
        }
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                cargarSeleccion();
            }
        });
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        cmbTipo.addActionListener(e -> ajustarCampoExtra());
        btnGuardar.addActionListener(e -> guardarNuevo());
        btnActualizar.addActionListener(e -> actualizar());
        btnLimpiar.addActionListener(e -> limpiar());
        btnRefrescar.addActionListener(e -> refrescar());
        ajustarCampoExtra();

        // POLIMORFISMO: el permiso depende de la subclase real (Administrador o Cajero)
        if (!usuario.puedeGestionarProductos()) {
            for (JComponent c : new JComponent[]{txtNombre, cmbCategoria, cmbTipo, txtPrecio, txtStock, txtExtra,
                btnGuardar, btnActualizar}) {
                c.setEnabled(false);
            }
            add(new JLabel("  Su rol (" + usuario.getRol() + ") solo permite consultar productos."), BorderLayout.SOUTH);
        }
    }

    private static void campo(JPanel p, String texto, JComponent c, int fila, int col) {
        campo(p, new JLabel(texto), c, fila, col);
    }

    private static void campo(JPanel p, JLabel etiqueta, JComponent c, int fila, int col) {
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 6, 4, 6);
        g.gridy = fila;
        g.anchor = GridBagConstraints.WEST;
        g.gridx = col * 2;
        p.add(etiqueta, g);
        g.gridx = col * 2 + 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;
        p.add(c, g);
    }

    /** El campo adicional cambia según el tipo de producto elegido. */
    private void ajustarCampoExtra() {
        String tipo = (String) cmbTipo.getSelectedItem();
        boolean perecedero = "Perecedero".equals(tipo);
        boolean electro = "Electrodoméstico".equals(tipo);
        lblExtra.setText(perecedero ? "Vence (aaaa-mm-dd):" : electro ? "Garantía (meses):" : "Dato adicional:");
        txtExtra.setEnabled(perecedero || electro);
        if (!(perecedero || electro)) {
            txtExtra.setText("");
        }
    }

    /**
     * Crea el objeto de la subclase que corresponde al tipo elegido. Las conversiones inválidas
     * se convierten en DatoInvalidoException (manejo de EXCEPCIONES).
     */
    private Producto construir(int codigo) throws DatoInvalidoException {
        String nombre = txtNombre.getText().trim();
        Object cat = cmbCategoria.getSelectedItem();
        String categoria = cat == null ? "" : cat.toString().trim();
        double precio;
        int stock;
        try {
            precio = Double.parseDouble(txtPrecio.getText().trim().replace(',', '.'));
            stock = Integer.parseInt(txtStock.getText().trim());
        } catch (NumberFormatException e) {
            throw new DatoInvalidoException("El precio y el stock deben ser números válidos.");
        }
        Producto p;
        switch ((String) cmbTipo.getSelectedItem()) {
            case "Canasta básica":
                p = new ProductoCanastaBasica(codigo, nombre, categoria, precio, stock);
                break;
            case "Perecedero":
                try {
                    p = new ProductoPerecedero(codigo, nombre, categoria, precio, stock,
                            LocalDate.parse(txtExtra.getText().trim()));
                } catch (DateTimeParseException e) {
                    throw new DatoInvalidoException("La fecha debe tener el formato aaaa-mm-dd.");
                }
                break;
            case "Electrodoméstico":
                try {
                    p = new ProductoElectrodomestico(codigo, nombre, categoria, precio, stock,
                            Integer.parseInt(txtExtra.getText().trim()));
                } catch (NumberFormatException e) {
                    throw new DatoInvalidoException("La garantía debe ser un número entero de meses.");
                }
                break;
            default:
                p = new ProductoGeneral(codigo, nombre, categoria, precio, stock);
        }
        p.validar(); // polimorfismo, cada subclase aplica sus propias reglas
        return p;
    }

    private void guardarNuevo() {
        if (seleccionado != null) {
            JOptionPane.showMessageDialog(this, "Presione 'Limpiar' para registrar un producto nuevo.",
                    "Productos", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        try {
            Producto p = construir(0);
            Tarea.ejecutar("PRODUCTO_CREAR", p, r -> {
                mostrar(r.isExito(), r.getMensaje());
                if (r.isExito()) {
                    limpiar();
                    refrescar();
                }
            });
        } catch (DatoInvalidoException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Datos inválidos", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void actualizar() {
        if (seleccionado == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un producto de la tabla.",
                    "Actualizar", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            Producto p = construir(seleccionado.getCodigo());
            Tarea.ejecutar("PRODUCTO_ACTUALIZAR", p, r -> {
                mostrar(r.isExito(), r.getMensaje());
                if (r.isExito()) {
                    refrescar();
                }
            });
        } catch (DatoInvalidoException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Datos inválidos", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void limpiar() {
        seleccionado = null;
        tabla.clearSelection();
        txtNombre.setText("");
        cmbCategoria.setSelectedIndex(0);
        txtPrecio.setText("");
        txtStock.setText("");
        txtExtra.setText("");
        cmbTipo.setEnabled(btnGuardar.isEnabled());
        ajustarCampoExtra();
    }

    private void cargarSeleccion() {
        int fila = tabla.getSelectedRow();
        if (fila < 0 || fila >= datos.size()) {
            return;
        }
        seleccionado = datos.get(fila);
        txtNombre.setText(seleccionado.getNombre());
        cmbCategoria.setSelectedItem(seleccionado.getCategoria());
        txtPrecio.setText(String.format(Locale.US, "%.2f", seleccionado.getPrecio()));
        txtStock.setText(String.valueOf(seleccionado.getStock()));
        cmbTipo.setSelectedItem(seleccionado.getTipo()); 
        cmbTipo.setEnabled(false); // el tipo de un producto registrado no se puede cambiar
        ajustarCampoExtra();
        if (seleccionado instanceof ProductoPerecedero) {
            txtExtra.setText(String.valueOf(((ProductoPerecedero) seleccionado).getFechaVencimiento()));
        } else if (seleccionado instanceof ProductoElectrodomestico) {
            txtExtra.setText(String.valueOf(((ProductoElectrodomestico) seleccionado).getGarantiaMeses()));
        }
    }

    private void mostrar(boolean exito, String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Productos",
                exito ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void refrescar() {
        Tarea.ejecutar("PRODUCTO_LISTAR", null, r -> {
            if (!r.isExito()) {
                mostrar(false, r.getMensaje());
                return;
            }
            datos = (ArrayList<Producto>) r.getDatos();
            modelo.setRowCount(0);
            for (Producto p : datos) {
                modelo.addRow(new Object[]{p.getCodigo(), p.getNombre(), p.getCategoria(), p.getTipo(),
                    String.format(Locale.US, "%.2f", p.getPrecio()),
                    String.format(Locale.US, "%.0f%%", p.getTasaImpuesto() * 100),
                    p.getStock(), p.getDetalle()});
            }
            seleccionado = null;
        });
    }
}
