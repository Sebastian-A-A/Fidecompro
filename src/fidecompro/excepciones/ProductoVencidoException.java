package fidecompro.excepciones;

import java.time.LocalDate;

/** Se lanza cuando se intenta vender un producto vencido. */
public class ProductoVencidoException extends FidecomproException {

    private static final long serialVersionUID = 1L;

    public ProductoVencidoException(String producto, LocalDate vencimiento) {
        super("El producto '" + producto + "' está vencido (venció el " + vencimiento + ").");
    }
}
