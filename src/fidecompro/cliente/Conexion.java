package fidecompro.cliente;

import fidecompro.comun.Respuesta;
import fidecompro.comun.Solicitud;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.Socket;

/** Conexión TCP con el servidor */
public class Conexion {

    private static final Conexion INSTANCIA = new Conexion();

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    /** Conexión compartida por la interfaz gráfica */
    public static Conexion getInstancia() {
        return INSTANCIA;
    }

    public synchronized void conectar(String host, int puerto) throws IOException {
        cerrar();
        socket = new Socket();
        socket.connect(new InetSocketAddress(host, puerto), 4000);
        out = new ObjectOutputStream(socket.getOutputStream());
        out.flush();
        in = new ObjectInputStream(socket.getInputStream());
    }

    public synchronized Respuesta enviar(String comando, Serializable dato) {
        try {
            if (socket == null || socket.isClosed()) {
                return new Respuesta(false, "No hay conexión con el servidor.", null);
            }
            out.reset();
            out.writeObject(new Solicitud(comando, dato));
            out.flush();
            return (Respuesta) in.readObject();
        } catch (Exception e) {
            return new Respuesta(false, "Error de comunicación con el servidor: " + e.getMessage(), null);
        }
    }

    public synchronized void cerrar() {
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException ignorado) {
        }
        socket = null;
    }
}
