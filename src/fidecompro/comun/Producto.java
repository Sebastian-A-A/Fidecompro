package fidecompro.comun;

import fidecompro.excepciones.DatoInvalidoException;
import fidecompro.excepciones.FidecomproException;
import fidecompro.excepciones.StockInsuficienteException;
import java.io.Serializable;
import java.util.Locale;

/**
 * Clase abstracta base de los productos 
 * Cada tipo define su propia tasa de impuesto y su detalle (POLIMORFISMO).
 */
public abstract class Producto implements Serializable {

    private static final long serialVersionUID = 1L;

    private int codigo;
    private String nombre;
    private String categoria;
    private double precio;
    private int stock;

    public Producto(int codigo, String nombre, String categoria, double precio, int stock) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.categoria = categoria;
        this.precio = precio;
        this.stock = stock;
    }

    public int getCodigo() { return codigo; }
    public void setCodigo(int codigo) { this.codigo = codigo; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }
    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    // ---- Métodos que cada subclase ----
    public abstract double getTasaImpuesto();
    public abstract String getTipo();
    public abstract Producto copia();

    public String getDetalle() {
        return "";
    }

    public double calcularImpuesto(int cantidad) {
        return precio * cantidad * getTasaImpuesto();
    }

    public void validar() throws DatoInvalidoException {
        if (nombre == null || nombre.trim().isEmpty() || categoria == null || categoria.trim().isEmpty()) {
            throw new DatoInvalidoException("El nombre y la categoría del producto son obligatorios.");
        }
        if (precio <= 0) {
            throw new DatoInvalidoException("El precio debe ser mayor que 0.");
        }
        if (stock < 0) {
            throw new DatoInvalidoException("El stock no puede ser negativo.");
        }
    }

    /** Verifica que se pueda vender la cantidad pedida, lanza una excepción si no es posible */
    public void verificarVenta(int cantidad) throws FidecomproException {
        if (stock < cantidad) {
            throw new StockInsuficienteException(nombre, stock, cantidad);
        }
    }

    public void reducirStock(int cantidad) {
        this.stock -= cantidad;
    }

    @Override
    public String toString() {
        return String.format(Locale.US, "%s - %.2f (stock %d)", nombre, precio, stock);
    }
}
