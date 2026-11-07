package fidecompro.comun;

/** Usuario que atiende la caja, factura y registra clientes pero no modifica el inventario. */
public class Cajero extends Usuario {

    private static final long serialVersionUID = 1L;

    public Cajero(String username, String passwordHash, String cedula, String nombre) {
        super(username, passwordHash, cedula, nombre);
    }

    @Override
    public String getRol() {
        return "CAJERO";
    }

    @Override
    public boolean puedeGestionarProductos() {
        return false;
    }

    @Override
    public boolean puedeCrearUsuarios() {
        return false;
    }

    @Override
    public Usuario copiaSinClave() {
        return new Cajero(getUsername(), "", getCedula(), getNombre());
    }
}
