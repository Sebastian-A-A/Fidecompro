package fidecompro.comun;

import fidecompro.excepciones.DatoInvalidoException;
import java.io.Serializable;

/**
 * Clase abstracta base, contiene lo que tienen en común las personas del sistema.
 */
public abstract class Persona implements Serializable {

    private static final long serialVersionUID = 1L;

    private String cedula;
    private String nombre;
    private String telefono;
    private String correo;

    public Persona(String cedula, String nombre, String telefono, String correo) {
        this.cedula = cedula;
        this.nombre = nombre;
        this.telefono = telefono;
        this.correo = correo;
    }

    public String getCedula() { return cedula; }
    public void setCedula(String cedula) { this.cedula = cedula; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    /** Cada subclase indica qué tipo de persona es */
    public abstract String getTipo();

    /** POLIMORFISMO por sobrescritura */
    public void validar() throws DatoInvalidoException {
        if (esVacio(cedula) || esVacio(nombre)) {
            throw new DatoInvalidoException("La cédula y el nombre son obligatorios.");
        }
    }

    public String describir() {
        return getTipo() + ": " + nombre + " (" + cedula + ")";
    }

    protected static boolean esVacio(String s) {
        return s == null || s.trim().isEmpty();
    }

    @Override
    public String toString() {
        return nombre + " (" + cedula + ")";
    }
}
