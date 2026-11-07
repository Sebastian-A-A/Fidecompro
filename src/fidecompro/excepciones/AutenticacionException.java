package fidecompro.excepciones;

/** Se lanza cuando el usuario o la contraseña son incorrectos. */
public class AutenticacionException extends FidecomproException {

    private static final long serialVersionUID = 1L;

    public AutenticacionException() {
        super("Usuario o contraseña incorrectos.");
    }
}
