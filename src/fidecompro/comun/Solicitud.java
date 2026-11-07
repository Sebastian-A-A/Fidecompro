package fidecompro.comun;

import java.io.Serializable;

/** Mensaje que el cliente envía al servidor */
public class Solicitud implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String comando;
    private final Serializable dato;

    public Solicitud(String comando, Serializable dato) {
        this.comando = comando;
        this.dato = dato;
    }

    public String getComando() { return comando; }
    public Serializable getDato() { return dato; }
}
