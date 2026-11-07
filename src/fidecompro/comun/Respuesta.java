package fidecompro.comun;

import java.io.Serializable;

/** Mensaje que el servidor devuelve al cliente */
public class Respuesta implements Serializable {

    private static final long serialVersionUID = 1L;

    private final boolean exito;
    private final String mensaje;
    private final Serializable datos;

    public Respuesta(boolean exito, String mensaje, Serializable datos) {
        this.exito = exito;
        this.mensaje = mensaje;
        this.datos = datos;
    }

    public boolean isExito() { return exito; }
    public String getMensaje() { return mensaje; }
    public Serializable getDatos() { return datos; }
}
