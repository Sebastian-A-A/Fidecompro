package fidecompro.comun;

/** Productos de la canasta básica, impuesto reducido al 1% */
public class ProductoCanastaBasica extends Producto {

    private static final long serialVersionUID = 1L;

    public ProductoCanastaBasica(int codigo, String nombre, String categoria, double precio, int stock) {
        super(codigo, nombre, categoria, precio, stock);
    }

    @Override
    public double getTasaImpuesto() {
        return 0.01;
    }

    @Override
    public String getTipo() {
        return "Canasta básica";
    }

    @Override
    public Producto copia() {
        return new ProductoCanastaBasica(getCodigo(), getNombre(), getCategoria(), getPrecio(), getStock());
    }
}
