package fidecompro.comun;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/** Usuario del sistema. */
public abstract class Usuario extends Persona {

    private static final long serialVersionUID = 1L;

    private final String username;
    private final String passwordHash;

    public Usuario(String username, String passwordHash, String cedula, String nombre) {
        super(cedula, nombre, "", "");
        this.username = username;
        this.passwordHash = passwordHash;
    }

    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }

    /** Rol del usuario (lo define cada subclase). */
    public abstract String getRol();

    /** Permiso que cambia según el rol */
    public abstract boolean puedeGestionarProductos();

    /** Otro permiso que cambia según el rol, solo el administrador crea usuarios. */
    public abstract boolean puedeCrearUsuarios();

    /** Copia sin la contraseña */
    public abstract Usuario copiaSinClave();

    @Override
    public String getTipo() {
        return "Usuario " + getRol();
    }

    /** Hash SHA-256 */
    public static String hash(String texto) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            StringBuilder sb = new StringBuilder();
            for (byte b : md.digest(texto.getBytes(StandardCharsets.UTF_8))) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("No se pudo calcular el hash", e);
        }
    }
}
