package fidecompro.comun;

import fidecompro.excepciones.DatoInvalidoException;

/** Electrodomésticos, impuesto del 13% y garantía en meses. */
public class ProductoElectrodomestico extends Producto {

    private static final long serialVersionUID = 1L;

    private int garantiaMeses;

    public ProductoElectrodomestico(int codigo, String nombre, String categoria, double precio, int stock,
            int garantiaMeses) {
        super(codigo, nombre, categoria, precio, stock);
        this.garantiaMeses = garantiaMeses;
    }

    public int getGarantiaMeses() { return garantiaMeses; }
    public void setGarantiaMeses(int garantiaMeses) { this.garantiaMeses = garantiaMeses; }

    @Override
    public double getTasaImpuesto() {
        return 0.13;
    }

    @Override
    public String getTipo() {
        return "Electrodoméstico";
    }

    @Override
    public String getDetalle() {
        return "Garantía: " + garantiaMeses + " meses";
    }

    @Override
    public void validar() throws DatoInvalidoException {
        super.validar();
        if (garantiaMeses < 0) {
            throw new DatoInvalidoException("La garantía no puede ser negativa.");
        }
    }

    @Override
    public Producto copia() {
        return new ProductoElectrodomestico(getCodigo(), getNombre(), getCategoria(), getPrecio(), getStock(),
                garantiaMeses);
    }
}
