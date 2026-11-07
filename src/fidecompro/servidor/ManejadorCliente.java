package fidecompro.servidor;

import fidecompro.comun.Cliente;
import fidecompro.comun.Factura;
import fidecompro.comun.LineaFactura;
import fidecompro.comun.Producto;
import fidecompro.comun.Respuesta;
import fidecompro.comun.Solicitud;
import fidecompro.comun.Usuario;
import fidecompro.excepciones.FidecomproException;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

/** MULTIHILOS: atiende a un cliente conectado y cada conexión corre en su propio hilo. */
public class ManejadorCliente implements Runnable {

    private final Socket socket;
    private final Repositorio repo;
    private Usuario usuarioActual;

    public ManejadorCliente(Socket socket, Repositorio repo) {
        this.socket = socket;
        this.repo = repo;
    }

    @Override
    public void run() {
        String origen = String.valueOf(socket.getRemoteSocketAddress());
        System.out.println("[+] Conexión de " + origen + " (hilo " + Thread.currentThread().getName() + ")");
        try {
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            while (true) {
                Solicitud solicitud = (Solicitud) in.readObject(); // SERIALIZACIÓN: se recibe un objeto
                Respuesta respuesta = procesar(solicitud);
                out.reset();
                out.writeObject(respuesta);                       // SERIALIZACIÓN: se envía un objeto
                out.flush();
            }
        } catch (EOFException | SocketException e) {
            // el cliente se desconectó
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error con " + origen + ": " + e);
        } finally {
            try {
                socket.close();
            } catch (IOException ignorado) {
            }
            System.out.println("[-] Desconectado " + origen
                    + (usuarioActual != null ? " (" + usuarioActual.getUsername() + ")" : ""));
        }
    }

    @SuppressWarnings("unchecked")
    private Respuesta procesar(Solicitud s) {
        String cmd = s.getComando();
        try {
            if ("LOGIN".equals(cmd)) {
                String[] d = (String[]) s.getDato();
                usuarioActual = repo.autenticar(d[0], d[1]);
                System.out.println("    Sesión iniciada: " + usuarioActual.describir());
                return new Respuesta(true, "Bienvenido, " + usuarioActual.getNombre(), usuarioActual);
            }
            if (usuarioActual == null) {
                return new Respuesta(false, "Debe iniciar sesión.", null);
            }
            switch (cmd) {
                case "CLIENTE_CREAR":
                    return new Respuesta(true, "Cliente registrado.", repo.crearCliente((Cliente) s.getDato()));
                case "CLIENTE_ACTUALIZAR":
                    repo.actualizarCliente((Cliente) s.getDato());
                    return new Respuesta(true, "Cliente actualizado.", null);
                case "CLIENTE_LISTAR":
                    return new Respuesta(true, "OK", repo.listarClientes());
                case "PRODUCTO_CREAR":
                    if (!usuarioActual.puedeGestionarProductos()) { // POLIMORFISMO depende del rol
                        return sinPermiso();
                    }
                    return new Respuesta(true, "Producto registrado.", repo.crearProducto((Producto) s.getDato()));
                case "PRODUCTO_ACTUALIZAR":
                    if (!usuarioActual.puedeGestionarProductos()) {
                        return sinPermiso();
                    }
                    repo.actualizarProducto((Producto) s.getDato());
                    return new Respuesta(true, "Producto actualizado.", null);
                case "PRODUCTO_LISTAR":
                    return new Respuesta(true, "OK", repo.listarProductos());
                case "FACTURA_CREAR": {
                    Object[] d = (Object[]) s.getDato();
                    int idCliente = (Integer) d[0];
                    ArrayList<LineaFactura> lineas = (ArrayList<LineaFactura>) d[1];
                    Factura f = repo.crearFactura(idCliente, lineas, usuarioActual.getUsername());
                    System.out.println("    Factura #" + f.getNumero() + " emitida por " + usuarioActual.getUsername());
                    return new Respuesta(true, "Factura #" + f.getNumero() + " emitida.", f);
                }
                case "FACTURA_LISTAR":
                    return new Respuesta(true, "OK", repo.listarFacturas());
                case "USUARIO_CREAR": {
                    if (!usuarioActual.puedeCrearUsuarios()) { // POLIMORFISMO depende del rol
                        return new Respuesta(false,
                                "Su rol (" + usuarioActual.getRol() + ") no permite crear usuarios.", null);
                    }
                    String[] d = (String[]) s.getDato();
                    repo.crearUsuario(d[0], d[1], d[2], d[3], d[4]);
                    return new Respuesta(true, "Usuario creado correctamente.", null);
                }
                default:
                    return new Respuesta(false, "Comando desconocido: " + cmd, null);
            }
        } catch (FidecomproException e) {
            // EXCEPCIONES de negocio, se informan al cliente con un mensaje claro
            return new Respuesta(false, e.getMessage(), null);
        } catch (ClassCastException | NullPointerException e) {
            return new Respuesta(false, "Solicitud inválida.", null);
        }
    }

    private Respuesta sinPermiso() {
        return new Respuesta(false, "Su rol (" + usuarioActual.getRol() + ") no permite modificar productos.", null);
    }
}
