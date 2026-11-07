package fidecompro.comun;

import fidecompro.excepciones.DatoInvalidoException;

/** Cliente al que se le emiten facturas, se hereda de Persona. */
public class Cliente extends Persona {

    private static final long serialVersionUID = 1L;

    private int id;
    private String direccion;

    public Cliente(int id, String cedula, String nombre, String telefono, String correo, String direccion) {
        super(cedula, nombre, telefono, correo);
        this.id = id;
        this.direccion = direccion;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    @Override
    public String getTipo() {
        return "Cliente";
    }

    @Override
    public void validar() throws DatoInvalidoException {
        super.validar();
        if (esVacio(getTelefono())) {
            throw new DatoInvalidoException("El teléfono del cliente es obligatorio.");
        }
    }

    @Override
    public String describir() {
        return super.describir() + " - Tel: " + getTelefono();
    }

    public Cliente copia() {
        return new Cliente(id, getCedula(), getNombre(), getTelefono(), getCorreo(), direccion);
    }
}
