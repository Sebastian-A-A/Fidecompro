package fidecompro.cliente;

import fidecompro.comun.Cliente;
import fidecompro.comun.Factura;
import fidecompro.comun.LineaFactura;
import fidecompro.comun.Producto;
import fidecompro.excepciones.FidecomproException;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Locale;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;

/** Creacion y emision de facturas*/
public class PanelFacturacion extends JPanel implements Refrescable {

    private final JComboBox<Cliente> cmbCliente = new JComboBox<>();
    private final JComboBox<Producto> cmbProducto = new JComboBox<>();
    private final JSpinner spnCantidad = new JSpinner(new SpinnerNumberModel(1, 1, 100000, 1));

    private final DefaultTableModel modelo = new DefaultTableModel(
            new String[]{"Código", "Producto", "Precio", "Cantidad", "IVA", "Subtotal"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int col) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modelo);
    private final ArrayList<LineaFactura> lineas = new ArrayList<>(); // COLECCIÓN de líneas de la factura

    private final JLabel lblSubtotal = new JLabel("Subtotal: 0.00");
    private final JLabel lblIva = new JLabel("Impuesto (IVA): 0.00");
    private final JLabel lblTotal = new JLabel("TOTAL: 0.00");

    public PanelFacturacion() {
        super(new BorderLayout(6, 6));

        JPanel norte = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 6, 4, 6);
        g.anchor = GridBagConstraints.WEST;
        g.gridy = 0;
        g.gridx = 0;
        norte.add(new JLabel("Cliente:"), g);
        g.gridx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;
        g.gridwidth = 3;
        norte.add(cmbCliente, g);
        g.gridy = 1;
        g.gridx = 0;
        g.gridwidth = 1;
        g.weightx = 0;
        g.fill = GridBagConstraints.NONE;
        norte.add(new JLabel("Producto:"), g);
        g.gridx = 1;
        g.gridwidth = 3;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;
        norte.add(cmbProducto, g);
        g.gridy = 2;
        g.gridx = 0;
        g.gridwidth = 1;
        g.weightx = 0;
        g.fill = GridBagConstraints.NONE;
        norte.add(new JLabel("Cantidad:"), g);
        g.gridx = 1;
        norte.add(spnCantidad, g);
        g.gridx = 2;
        JButton btnAgregar = new JButton("Agregar a la factura");
        norte.add(btnAgregar, g);
        add(norte, BorderLayout.NORTH);

        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        lblTotal.setFont(lblTotal.getFont().deriveFont(Font.BOLD, 15f));

        JButton btnQuitar = new JButton("Quitar línea");
        JButton btnGenerar = new JButton("Generar factura");
        JPanel izquierda = new JPanel(new FlowLayout(FlowLayout.LEFT));
        izquierda.add(btnQuitar);
        JPanel totales = new JPanel(new GridBagLayout());
        GridBagConstraints t = new GridBagConstraints();
        t.gridx = 0;
        t.anchor = GridBagConstraints.EAST;
        t.insets = new Insets(2, 6, 2, 6);
        t.gridy = 0;
        totales.add(lblSubtotal, t);
        t.gridy = 1;
        totales.add(lblIva, t);
        t.gridy = 2;
        totales.add(lblTotal, t);
        t.gridy = 3;
        totales.add(btnGenerar, t);
        JPanel sur = new JPanel(new BorderLayout());
        sur.add(izquierda, BorderLayout.WEST);
        sur.add(totales, BorderLayout.EAST);
        add(sur, BorderLayout.SOUTH);

        btnAgregar.addActionListener(e -> agregar());
        btnQuitar.addActionListener(e -> quitar());
        btnGenerar.addActionListener(e -> generar());
    }

    private void agregar() {
        Producto p = (Producto) cmbProducto.getSelectedItem();
        if (p == null) {
            JOptionPane.showMessageDialog(this, "No hay productos disponibles.",
                    "Facturación", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int cantidad = (Integer) spnCantidad.getValue();
        LineaFactura existente = null;
        for (LineaFactura l : lineas) {
            if (l.getCodigoProducto() == p.getCodigo()) {
                existente = l;
            }
        }
        int yaAgregado = existente == null ? 0 : existente.getCantidad();
        try {
            // POLIMORFISMO + EXCEPCIONES: un perecedero también revisa su fecha de vencimiento
            p.verificarVenta(yaAgregado + cantidad);
        } catch (FidecomproException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "No se puede agregar", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (existente != null) {
            existente.setCantidad(yaAgregado + cantidad);
        } else {
            lineas.add(new LineaFactura(p.getCodigo(), p.getNombre(), p.getPrecio(), cantidad, p.getTasaImpuesto()));
        }
        spnCantidad.setValue(1);
        recargarTabla();
    }

    private void quitar() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione la línea que desea quitar.",
                    "Facturación", JOptionPane.WARNING_MESSAGE);
            return;
        }
        lineas.remove(fila);
        recargarTabla();
    }

    private void recargarTabla() {
        modelo.setRowCount(0);
        double subtotal = 0;
        double impuesto = 0;
        for (LineaFactura l : lineas) {
            modelo.addRow(new Object[]{l.getCodigoProducto(), l.getNombreProducto(),
                String.format(Locale.US, "%.2f", l.getPrecioUnitario()), l.getCantidad(),
                String.format(Locale.US, "%.0f%%", l.getTasaImpuesto() * 100),
                String.format(Locale.US, "%.2f", l.getSubtotal())});
            subtotal += l.getSubtotal();
            impuesto += l.getImpuesto();
        }
        lblSubtotal.setText(String.format(Locale.US, "Subtotal: %.2f", subtotal));
        lblIva.setText(String.format(Locale.US, "Impuesto (IVA): %.2f", impuesto));
        lblTotal.setText(String.format(Locale.US, "TOTAL: %.2f", subtotal + impuesto));
    }

    private void generar() {
        Cliente c = (Cliente) cmbCliente.getSelectedItem();
        if (c == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un cliente (regístrelo primero si no existe).",
                    "Facturación", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (lineas.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Agregue al menos un producto a la factura.",
                    "Facturación", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Object[] datos = {c.getId(), new ArrayList<>(lineas)};
        Tarea.ejecutar("FACTURA_CREAR", datos, r -> {
            if (r.isExito()) {
                lineas.clear();
                recargarTabla();
                refrescar();
                mostrarFactura(this, (Factura) r.getDatos());
            } else {
                JOptionPane.showMessageDialog(this, r.getMensaje(), "No se pudo emitir la factura",
                        JOptionPane.ERROR_MESSAGE);
                refrescar(); // el stock pudo cambiar por otro cajero
            }
        });
    }

    /** Muestra la factura y permite guardarla como archivo .txt */
    public static void mostrarFactura(Component padre, Factura f) {
        JTextArea area = new JTextArea(f.generarTexto(), 22, 50);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        area.setEditable(false);
        Object[] opciones = {"Guardar archivo...", "Cerrar"};
        int op = JOptionPane.showOptionDialog(padre, new JScrollPane(area), "Factura #" + f.getNumero(),
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, opciones, opciones[0]);
        if (op != 0) {
            return;
        }
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File(String.format("factura_%04d.txt", f.getNumero())));
        if (fc.showSaveDialog(padre) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        try {
            Files.write(fc.getSelectedFile().toPath(), f.generarTexto().getBytes(StandardCharsets.UTF_8));
            JOptionPane.showMessageDialog(padre, "Factura guardada en:\n" + fc.getSelectedFile().getAbsolutePath(),
                    "Factura guardada", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(padre, "No se pudo guardar el archivo: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void refrescar() {
        final Cliente previo = (Cliente) cmbCliente.getSelectedItem();
        Tarea.ejecutar("CLIENTE_LISTAR", null, r -> {
            if (!r.isExito()) {
                return;
            }
            cmbCliente.removeAllItems();
            Cliente reseleccion = null;
            for (Cliente c : (ArrayList<Cliente>) r.getDatos()) {
                cmbCliente.addItem(c);
                if (previo != null && previo.getId() == c.getId()) {
                    reseleccion = c;
                }
            }
            if (reseleccion != null) {
                cmbCliente.setSelectedItem(reseleccion);
            }
        });
        Tarea.ejecutar("PRODUCTO_LISTAR", null, r -> {
            if (!r.isExito()) {
                return;
            }
            cmbProducto.removeAllItems();
            for (Producto p : (ArrayList<Producto>) r.getDatos()) {
                if (p.getStock() > 0) {
                    cmbProducto.addItem(p);
                }
            }
        });
    }
}
