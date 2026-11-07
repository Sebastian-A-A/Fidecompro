package fidecompro.comun;

import java.io.Serializable;

/** Línea producto y cantidad dentro de una factura. Guarda la tasa de impuesto del producto. */
public class LineaFactura implements Serializable {

    private static final long serialVersionUID = 1L;

    private final int codigoProducto;
    private final String nombreProducto;
    private final double precioUnitario;
    private int cantidad;
    private final double tasaImpuesto;

    public LineaFactura(int codigoProducto, String nombreProducto, double precioUnitario, int cantidad,
            double tasaImpuesto) {
        this.codigoProducto = codigoProducto;
        this.nombreProducto = nombreProducto;
        this.precioUnitario = precioUnitario;
        this.cantidad = cantidad;
        this.tasaImpuesto = tasaImpuesto;
    }

    public int getCodigoProducto() { return codigoProducto; }
    public String getNombreProducto() { return nombreProducto; }
    public double getPrecioUnitario() { return precioUnitario; }
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
    public double getTasaImpuesto() { return tasaImpuesto; }

    public double getSubtotal() { return precioUnitario * cantidad; }
    public double getImpuesto() { return getSubtotal() * tasaImpuesto; }
}
