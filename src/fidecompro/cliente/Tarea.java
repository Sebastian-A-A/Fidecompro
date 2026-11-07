package fidecompro.cliente;

import fidecompro.comun.Respuesta;
import java.io.Serializable;
import java.util.function.Consumer;
import javax.swing.SwingWorker;

/**
 * Ejecuta una petición al servidor en un hilo de fondo para no congelar la interfaz
 */
public class Tarea {

    public static void ejecutar(String comando, Serializable dato, Consumer<Respuesta> alTerminar) {
        new SwingWorker<Respuesta, Void>() {
            @Override
            protected Respuesta doInBackground() {
                return Conexion.getInstancia().enviar(comando, dato);
            }

            @Override
            protected void done() {
                Respuesta r;
                try {
                    r = get();
                } catch (Exception e) {
                    r = new Respuesta(false, "Error: " + e.getMessage(), null);
                }
                alTerminar.accept(r);
            }
        }.execute();
    }
}
