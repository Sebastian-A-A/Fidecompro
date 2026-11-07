package fidecompro.excepciones;

/** Excepción base del sistema, todas las excepciones de negocio se heredan */
public class FidecomproException extends Exception {

    private static final long serialVersionUID = 1L;

    public FidecomproException(String mensaje) {
        super(mensaje);
    }
}
