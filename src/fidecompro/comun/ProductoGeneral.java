package fidecompro.comun;

/** Productos de uso general, impuesto del 13%. */
public class ProductoGeneral extends Producto {

    private static final long serialVersionUID = 1L;

    public ProductoGeneral(int codigo, String nombre, String categoria, double precio, int stock) {
        super(codigo, nombre, categoria, precio, stock);
    }

    @Override
    public double getTasaImpuesto() {
        return 0.13;
    }

    @Override
    public String getTipo() {
        return "General";
    }

    @Override
    public Producto copia() {
        return new ProductoGeneral(getCodigo(), getNombre(), getCategoria(), getPrecio(), getStock());
    }
}
