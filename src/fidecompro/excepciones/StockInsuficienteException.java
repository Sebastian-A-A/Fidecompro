package fidecompro.excepciones;

/** Se lanza cuando se intenta vender más unidades de las que hay en inventario. */
public class StockInsuficienteException extends FidecomproException {

    private static final long serialVersionUID = 1L;

    private final int disponible;

    public StockInsuficienteException(String producto, int disponible, int solicitado) {
        super("Stock insuficiente de '" + producto + "'. Disponible: " + disponible
                + ", solicitado: " + solicitado + ".");
        this.disponible = disponible;
    }

    public int getDisponible() {
        return disponible;
    }
}
