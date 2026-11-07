package fidecompro.servidor;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Punto de entrada del servidor. */
public class Servidor {

    public static void main(String[] args) throws IOException {
        int puerto = args.length > 0 ? Integer.parseInt(args[0]) : 5000;
        Repositorio repo = new Repositorio();
        ExecutorService pool = Executors.newCachedThreadPool(); // un hilo por cliente conectado

        try (ServerSocket servidor = new ServerSocket(puerto)) {
            System.out.println("Servidor Fidecompro escuchando en el puerto " + puerto + "...");
            while (true) {
                Socket socket = servidor.accept();
                pool.execute(new ManejadorCliente(socket, repo));
            }
        } finally {
            pool.shutdown();
        }
    }
}
