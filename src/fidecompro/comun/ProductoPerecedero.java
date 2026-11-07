package fidecompro.comun;

import fidecompro.excepciones.DatoInvalidoException;
import fidecompro.excepciones.FidecomproException;
import fidecompro.excepciones.ProductoVencidoException;
import java.time.LocalDate;

/** Productos con fecha de vencimiento, no se pueden vender si están vencidos */
public class ProductoPerecedero extends Producto {

    private static final long serialVersionUID = 1L;

    private LocalDate fechaVencimiento;

    public ProductoPerecedero(int codigo, String nombre, String categoria, double precio, int stock,
            LocalDate fechaVencimiento) {
        super(codigo, nombre, categoria, precio, stock);
        this.fechaVencimiento = fechaVencimiento;
    }

    public LocalDate getFechaVencimiento() { return fechaVencimiento; }
    public void setFechaVencimiento(LocalDate fecha) { this.fechaVencimiento = fecha; }

    public boolean estaVencido() {
        return fechaVencimiento != null && fechaVencimiento.isBefore(LocalDate.now());
    }

    @Override
    public double getTasaImpuesto() {
        return 0.13;
    }

    @Override
    public String getTipo() {
        return "Perecedero";
    }

    @Override
    public String getDetalle() {
        return "Vence: " + fechaVencimiento;
    }

    @Override
    public void validar() throws DatoInvalidoException {
        super.validar();
        if (fechaVencimiento == null) {
            throw new DatoInvalidoException("Debe indicar la fecha de vencimiento.");
        }
    }

    @Override
    public void verificarVenta(int cantidad) throws FidecomproException {
        if (estaVencido()) {
            throw new ProductoVencidoException(getNombre(), fechaVencimiento);
        }
        super.verificarVenta(cantidad);
    }

    @Override
    public Producto copia() {
        return new ProductoPerecedero(getCodigo(), getNombre(), getCategoria(), getPrecio(), getStock(),
                fechaVencimiento);
    }
}
