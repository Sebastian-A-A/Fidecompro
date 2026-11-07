package fidecompro.servidor;

import fidecompro.comun.Administrador;
import fidecompro.comun.Cajero;
import fidecompro.comun.Cliente;
import fidecompro.comun.Factura;
import fidecompro.comun.LineaFactura;
import fidecompro.comun.Producto;
import fidecompro.comun.ProductoCanastaBasica;
import fidecompro.comun.ProductoElectrodomestico;
import fidecompro.comun.ProductoGeneral;
import fidecompro.comun.ProductoPerecedero;
import fidecompro.comun.Usuario;
import fidecompro.excepciones.AutenticacionException;
import fidecompro.excepciones.DatoInvalidoException;
import fidecompro.excepciones.FidecomproException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Almacén de datos compartido por todos los hilos del servidor.
 * COLECCIONES
 * SERIALIZACIÓN
 * MULTIHILOS
 */
public class Repositorio {

    private static final String DIR_DATOS = "datos";
    private static final String DIR_FACTURAS = "facturas";

    private HashMap<String, Usuario> usuarios;
    private HashMap<Integer, Producto> productos;
    private ArrayList<Cliente> clientes;
    private ArrayList<Factura> facturas;

    private int sigClienteId = 1;
    private int sigProductoCodigo = 1;
    private int sigFactura = 1;

    @SuppressWarnings("unchecked")
    public Repositorio() {
        new File(DIR_DATOS).mkdirs();
        new File(DIR_FACTURAS).mkdirs();
        usuarios = (HashMap<String, Usuario>) leer("usuarios.dat", new HashMap<String, Usuario>());
        productos = (HashMap<Integer, Producto>) leer("productos.dat", new HashMap<Integer, Producto>());
        clientes = (ArrayList<Cliente>) leer("clientes.dat", new ArrayList<Cliente>());
        facturas = (ArrayList<Factura>) leer("facturas.dat", new ArrayList<Factura>());

        for (Cliente c : clientes) {
            sigClienteId = Math.max(sigClienteId, c.getId() + 1);
        }
        for (Integer codigo : productos.keySet()) {
            sigProductoCodigo = Math.max(sigProductoCodigo, codigo + 1);
        }
        for (Factura f : facturas) {
            sigFactura = Math.max(sigFactura, f.getNumero() + 1);
        }
        if (usuarios.isEmpty()) {
            usuarios.put("admin", new Administrador("admin", Usuario.hash("admin123"), "0-0000-0000", "Administrador"));
            usuarios.put("cajero1", new Cajero("cajero1", Usuario.hash("cajero123"), "1-0000-0001", "Cajero Uno"));
            System.out.println("Usuarios iniciales -> admin / admin123  y  cajero1 / cajero123");
        }
        if (productos.isEmpty() && clientes.isEmpty()) {
            cargarDatosDeEjemplo();
        }
        guardar();
    }

    private void cargarDatosDeEjemplo() {
        try {
            crearCliente(new Cliente(0, "1-1111-1111", "Ana Mora", "8888-0000", "ana@gmail.com", "San José"));
            crearCliente(new Cliente(0, "2-2222-2222", "Luis Soto", "7777-1111", "luis@gmail.com", "Heredia"));
            crearCliente(new Cliente(0, "3-3333-3333", "Comercial La Esquina S.A.", "2222-3333",
                    "ventas@laesquina.cr", "Alajuela"));
            crearProducto(new ProductoCanastaBasica(0, "Arroz 1 kg", "Abarrotes", 1500, 200));
            crearProducto(new ProductoCanastaBasica(0, "Frijoles 1 kg", "Abarrotes", 1900, 120));
            crearProducto(new ProductoPerecedero(0, "Leche entera 1 L", "Lácteos", 1100, 150,
                    LocalDate.now().plusMonths(2)));
            crearProducto(new ProductoPerecedero(0, "Yogur natural 1 L", "Lácteos", 1800, 20,
                    LocalDate.now().minusDays(3)));
            crearProducto(new ProductoGeneral(0, "Detergente 2 L", "Limpieza", 3200, 80));
            crearProducto(new ProductoGeneral(0, "Refresco cola 2 L", "Bebidas", 1900, 120));
            crearProducto(new ProductoElectrodomestico(0, "Licuadora 600 W", "Electrodomésticos", 24500, 15, 12));
        } catch (FidecomproException e) {
            System.err.println("No se pudieron cargar los datos de ejemplo: " + e.getMessage());
        }
    }

    // -Usuarios

    /** Devuelve el usuario sin clave o lanza AutenticacionException. */
    public synchronized Usuario autenticar(String username, String password) throws AutenticacionException {
        Usuario u = (username == null || password == null) ? null : usuarios.get(username);
        if (u == null || !u.getPasswordHash().equals(Usuario.hash(password))) {
            throw new AutenticacionException();
        }
        return u.copiaSinClave();
    }

    /** Crea un usuario nuevo, la subclase depende del rol elegido(Administrador o Cajero)*/
    public synchronized void crearUsuario(String username, String password, String nombre, String cedula,
            String rol) throws DatoInvalidoException {
        if (username == null || username.trim().isEmpty() || nombre == null || nombre.trim().isEmpty()
                || cedula == null || cedula.trim().isEmpty()) {
            throw new DatoInvalidoException("Usuario, nombre y cédula son obligatorios.");
        }
        if (password == null || password.length() < 4) {
            throw new DatoInvalidoException("La contraseña debe tener al menos 4 caracteres.");
        }
        for (String existente : usuarios.keySet()) {
            if (existente.equalsIgnoreCase(username.trim())) {
                throw new DatoInvalidoException("Ya existe un usuario con ese nombre.");
            }
        }
        String user = username.trim();
        String hash = Usuario.hash(password);
        Usuario nuevo;
        if ("ADMIN".equals(rol)) {
            nuevo = new Administrador(user, hash, cedula.trim(), nombre.trim());
        } else if ("CAJERO".equals(rol)) {
            nuevo = new Cajero(user, hash, cedula.trim(), nombre.trim());
        } else {
            throw new DatoInvalidoException("Rol inválido.");
        }
        usuarios.put(user, nuevo);
        guardar();
    }

    // -Clientes

    public synchronized Cliente crearCliente(Cliente c) throws DatoInvalidoException {
        c.validar();
        for (Cliente x : clientes) {
            if (x.getCedula().equalsIgnoreCase(c.getCedula().trim())) {
                throw new DatoInvalidoException("Ya existe un cliente con esa cédula.");
            }
        }
        Cliente nuevo = new Cliente(sigClienteId++, c.getCedula().trim(), c.getNombre().trim(),
                c.getTelefono().trim(), nz(c.getCorreo()), nz(c.getDireccion()));
        clientes.add(nuevo);
        guardar();
        return nuevo.copia();
    }

    public synchronized void actualizarCliente(Cliente c) throws DatoInvalidoException {
        c.validar();
        Cliente actual = null;
        for (Cliente x : clientes) {
            if (x.getId() == c.getId()) {
                actual = x;
            } else if (x.getCedula().equalsIgnoreCase(c.getCedula().trim())) {
                throw new DatoInvalidoException("Otro cliente ya tiene esa cédula.");
            }
        }
        if (actual == null) {
            throw new DatoInvalidoException("El cliente no existe.");
        }
        actual.setCedula(c.getCedula().trim());
        actual.setNombre(c.getNombre().trim());
        actual.setTelefono(c.getTelefono().trim());
        actual.setCorreo(nz(c.getCorreo()));
        actual.setDireccion(nz(c.getDireccion()));
        guardar();
    }

    /** Lista de clientes ordenada por nombre*/
    public synchronized ArrayList<Cliente> listarClientes() {
        ArrayList<Cliente> copia = new ArrayList<>();
        for (Cliente c : clientes) {
            copia.add(c.copia());
        }
        copia.sort(Comparator.comparing(Cliente::getNombre, String.CASE_INSENSITIVE_ORDER));
        return copia;
    }

    // -Productos

    public synchronized Producto crearProducto(Producto p) throws DatoInvalidoException {
        p.validar(); // POLIMORFISMO, cada tipo de producto se valida
        p.setCodigo(sigProductoCodigo++);
        productos.put(p.getCodigo(), p.copia());
        guardar();
        return p.copia();
    }

    public synchronized void actualizarProducto(Producto p) throws DatoInvalidoException {
        p.validar();
        Producto actual = productos.get(p.getCodigo());
        if (actual == null) {
            throw new DatoInvalidoException("El producto no existe.");
        }
        if (actual.getClass() != p.getClass()) {
            throw new DatoInvalidoException("No se puede cambiar el tipo de un producto ya registrado.");
        }
        productos.put(p.getCodigo(), p.copia());
        guardar();
    }

    public synchronized ArrayList<Producto> listarProductos() {
        ArrayList<Producto> copia = new ArrayList<>();
        for (Producto p : productos.values()) {
            copia.add(p.copia());
        }
        copia.sort(Comparator.comparingInt(Producto::getCodigo));
        return copia;
    }

    // -Facturas

    /**
     * Crea una factura. Validar, descontar stock, numerar y guardar pasa en un solo método synchronized
     */
    public synchronized Factura crearFactura(int idCliente, List<LineaFactura> pedidas, String usuario)
            throws FidecomproException {
        Cliente cliente = null;
        for (Cliente c : clientes) {
            if (c.getId() == idCliente) {
                cliente = c;
            }
        }
        if (cliente == null) {
            throw new DatoInvalidoException("El cliente no existe.");
        }
        if (pedidas == null || pedidas.isEmpty()) {
            throw new DatoInvalidoException("La factura no tiene productos.");
        }

        // Se agrupan cantidades por producto
        Map<Integer, Integer> totales = new LinkedHashMap<>();
        for (LineaFactura l : pedidas) {
            if (l.getCantidad() <= 0) {
                throw new DatoInvalidoException("Las cantidades deben ser mayores que 0.");
            }
            totales.merge(l.getCodigoProducto(), l.getCantidad(), Integer::sum);
        }

        // 1) Validar todo antes de modificar 
        for (Map.Entry<Integer, Integer> e : totales.entrySet()) {
            Producto p = productos.get(e.getKey());
            if (p == null) {
                throw new DatoInvalidoException("El producto con código " + e.getKey() + " no existe.");
            }
            p.verificarVenta(e.getValue()); // POLIMORFISMO: cada tipo verifica a su manera
        }

        // 2) Descuenta inventario y arma la factura 
        Factura f = new Factura(sigFactura++, LocalDateTime.now(), cliente.copia(), usuario);
        for (Map.Entry<Integer, Integer> e : totales.entrySet()) {
            Producto p = productos.get(e.getKey());
            p.reducirStock(e.getValue());
            f.agregarLinea(new LineaFactura(p.getCodigo(), p.getNombre(), p.getPrecio(), e.getValue(),
                    p.getTasaImpuesto()));
        }
        facturas.add(f);
        guardar();
        escribirArchivoFactura(f);
        return f;
    }

    public synchronized ArrayList<Factura> listarFacturas() {
        return new ArrayList<>(facturas);
    }

    private void escribirArchivoFactura(Factura f) {
        try {
            String nombre = String.format("factura_%04d.txt", f.getNumero());
            Files.write(Paths.get(DIR_FACTURAS, nombre), f.generarTexto().getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            System.err.println("No se pudo escribir el archivo de la factura: " + e.getMessage());
        }
    }

    // -Persistencia

    private void guardar() {
        escribir("usuarios.dat", usuarios);
        escribir("productos.dat", productos);
        escribir("clientes.dat", clientes);
        escribir("facturas.dat", facturas);
    }

    private void escribir(String nombre, Object coleccion) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(new File(DIR_DATOS, nombre)))) {
            out.writeObject(coleccion);
        } catch (Exception e) {
            System.err.println("Error guardando " + nombre + ": " + e.getMessage());
        }
    }

    private Object leer(String nombre, Object porDefecto) {
        File archivo = new File(DIR_DATOS, nombre);
        if (!archivo.exists()) {
            return porDefecto;
        }
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(archivo))) {
            return in.readObject();
        } catch (Exception e) {
            System.err.println("Error leyendo " + nombre + ": " + e.getMessage());
            return porDefecto;
        }
    }

    private static String nz(String s) {
        return s == null ? "" : s.trim();
    }
}
