package fidecompro.cliente;

import fidecompro.comun.Factura;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

/** Consulta de facturas emitidas y exportación de la factura física */
public class PanelHistorial extends JPanel implements Refrescable {

    private final DefaultTableModel modelo = new DefaultTableModel(
            new String[]{"N°", "Fecha", "Cliente", "Cajero", "Total"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int col) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modelo);
    private ArrayList<Factura> datos = new ArrayList<>();

    public PanelHistorial() {
        super(new BorderLayout(6, 6));
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        JButton btnVer = new JButton("Ver / Exportar factura");
        JButton btnRefrescar = new JButton("Refrescar");
        JPanel sur = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 6));
        sur.add(btnVer);
        sur.add(btnRefrescar);
        add(sur, BorderLayout.SOUTH);

        btnVer.addActionListener(e -> {
            int fila = tabla.getSelectedRow();
            if (fila < 0 || fila >= datos.size()) {
                JOptionPane.showMessageDialog(this, "Seleccione una factura de la tabla.",
                        "Historial", JOptionPane.WARNING_MESSAGE);
                return;
            }
            PanelFacturacion.mostrarFactura(this, datos.get(fila));
        });
        btnRefrescar.addActionListener(e -> refrescar());
    }

    @Override
    @SuppressWarnings("unchecked")
    public void refrescar() {
        Tarea.ejecutar("FACTURA_LISTAR", null, r -> {
            if (!r.isExito()) {
                JOptionPane.showMessageDialog(this, r.getMensaje(), "Historial", JOptionPane.ERROR_MESSAGE);
                return;
            }
            datos = (ArrayList<Factura>) r.getDatos();
            modelo.setRowCount(0);
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            for (Factura f : datos) {
                modelo.addRow(new Object[]{f.getNumero(), f.getFecha().format(fmt), f.getCliente().getNombre(),
                    f.getUsuario(), String.format(Locale.US, "%.2f", f.getTotal())});
            }
        });
    }
}
