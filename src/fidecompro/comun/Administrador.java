package fidecompro.comun;

/** Usuario con permisos completos. */
public class Administrador extends Usuario {

    private static final long serialVersionUID = 1L;

    public Administrador(String username, String passwordHash, String cedula, String nombre) {
        super(username, passwordHash, cedula, nombre);
    }

    @Override
    public String getRol() {
        return "ADMIN";
    }

    @Override
    public boolean puedeGestionarProductos() {
        return true;
    }

    @Override
    public boolean puedeCrearUsuarios() {
        return true;
    }

    @Override
    public Usuario copiaSinClave() {
        return new Administrador(getUsername(), "", getCedula(), getNombre());
    }
}
