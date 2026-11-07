package fidecompro.excepciones;

/** Se lanza cuando un dato ingresado no cumple las reglas como campo vacío o precio negativo */
public class DatoInvalidoException extends FidecomproException {

    private static final long serialVersionUID = 1L;

    public DatoInvalidoException(String mensaje) {
        super(mensaje);
    }
}
