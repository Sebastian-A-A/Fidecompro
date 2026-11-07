package fidecompro.comun;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Factura emitida a un cliente*/
public class Factura implements Serializable {

    private static final long serialVersionUID = 1L;

    private final int numero;
    private final LocalDateTime fecha;
    private final Cliente cliente;
    private final String usuario;
    private final ArrayList<LineaFactura> lineas = new ArrayList<>();

    public Factura(int numero, LocalDateTime fecha, Cliente cliente, String usuario) {
        this.numero = numero;
        this.fecha = fecha;
        this.cliente = cliente;
        this.usuario = usuario;
    }

    public int getNumero() { return numero; }
    public LocalDateTime getFecha() { return fecha; }
    public Cliente getCliente() { return cliente; }
    public String getUsuario() { return usuario; }
    public List<LineaFactura> getLineas() { return lineas; }

    public void agregarLinea(LineaFactura linea) {
        lineas.add(linea);
    }

    public double getSubtotal() {
        double s = 0;
        for (LineaFactura l : lineas) {
            s += l.getSubtotal();
        }
        return s;
    }

    public double getImpuesto() {
        double s = 0;
        for (LineaFactura l : lineas) {
            s += l.getImpuesto();
        }
        return s;
    }

    public double getTotal() {
        return getSubtotal() + getImpuesto();
    }

    /** Desglose de pago, contenido de la factura */
    public String generarTexto() {
        final String nl = System.lineSeparator();
        final String igual = "===============================================" + nl;
        final String guion = "-----------------------------------------------" + nl;
        DateTimeFormatter f = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        StringBuilder sb = new StringBuilder();
        sb.append(igual);
        sb.append(String.format(Locale.US, "          FIDECOMPRO - FACTURA #%04d%n", numero));
        sb.append(igual);
        sb.append("Fecha:   ").append(fecha.format(f)).append(nl);
        sb.append("Cliente: ").append(cliente.getNombre()).append(nl);
        sb.append("Cedula:  ").append(cliente.getCedula()).append(nl);
        sb.append("Cajero:  ").append(usuario).append(nl);
        sb.append(guion);
        sb.append(String.format(Locale.US, "%-16s %4s %9s %4s %10s%n", "Producto", "Cant", "Precio", "IVA", "Subtotal"));
        for (LineaFactura l : lineas) {
            String nombre = l.getNombreProducto();
            if (nombre.length() > 16) {
                nombre = nombre.substring(0, 16);
            }
            sb.append(String.format(Locale.US, "%-16s %4d %9.2f %3.0f%% %10.2f%n",
                    nombre, l.getCantidad(), l.getPrecioUnitario(), l.getTasaImpuesto() * 100, l.getSubtotal()));
        }
        sb.append(guion);
        sb.append(String.format(Locale.US, "%-36s %10.2f%n", "Subtotal:", getSubtotal()));
        sb.append(String.format(Locale.US, "%-36s %10.2f%n", "Impuesto (IVA):", getImpuesto()));
        sb.append(String.format(Locale.US, "%-36s %10.2f%n", "TOTAL:", getTotal()));
        sb.append(igual);
        sb.append("            Gracias por su compra").append(nl);
        return sb.toString();
    }
}
