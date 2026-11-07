package fidecompro.cliente;

import fidecompro.comun.Usuario;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

/** Ventana principal con las pestañas de Clientes, Productos, Facturación e Historial. */
public class VentanaPrincipal extends JFrame {

    private final Usuario usuario;
    private final JTabbedPane pestanas = new JTabbedPane();

    public VentanaPrincipal(Usuario usuario) {
        super("Fidecompro - Facturación");
        this.usuario = usuario;
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                salir();
            }
        });

        setJMenuBar(crearMenu());

        pestanas.addTab("Clientes", new PanelClientes());
        pestanas.addTab("Productos", new PanelProductos(usuario));
        pestanas.addTab("Facturación", new PanelFacturacion());
        pestanas.addTab("Historial", new PanelHistorial());
        pestanas.addChangeListener(e -> refrescarActual());

        // Polimorfismo: devuelve ADMIN o CAJERO según la subclase real del objeto
        JLabel lblUsuario = new JLabel("Usuario: " + usuario.getNombre() + " (" + usuario.getRol() + ")");
        lblUsuario.setBorder(new EmptyBorder(4, 8, 4, 8));

        add(pestanas, BorderLayout.CENTER);
        add(lblUsuario, BorderLayout.SOUTH);

        setPreferredSize(new Dimension(980, 640));
        pack();
        setLocationRelativeTo(null);
        refrescarActual();
    }

    private JMenuBar crearMenu() {
        JMenuBar barra = new JMenuBar();
        JMenu archivo = new JMenu("Archivo");

        JMenuItem nuevoUsuario = new JMenuItem("Nuevo usuario...");
        nuevoUsuario.setEnabled(usuario.puedeCrearUsuarios()); // POLIMORFISMO: depende del rol
        nuevoUsuario.addActionListener(e -> crearUsuario());

        JMenuItem cerrarSesion = new JMenuItem("Cerrar sesión");
        cerrarSesion.addActionListener(e -> {
            Conexion.getInstancia().cerrar();
            new VentanaLogin().setVisible(true);
            dispose();
        });
        JMenuItem salir = new JMenuItem("Salir");
        salir.addActionListener(e -> salir());

        archivo.add(nuevoUsuario);
        archivo.addSeparator();
        archivo.add(cerrarSesion);
        archivo.add(salir);
        barra.add(archivo);
        return barra;
    }

    private void refrescarActual() {
        if (pestanas.getSelectedComponent() instanceof Refrescable) {
            ((Refrescable) pestanas.getSelectedComponent()).refrescar();
        }
    }

    private void crearUsuario() {
        JTextField txtUser = new JTextField(12);
        JPasswordField txtPass = new JPasswordField(12);
        JTextField txtNombre = new JTextField(12);
        JTextField txtCedula = new JTextField(12);
        JComboBox<String> cmbRol = new JComboBox<>(new String[]{"CAJERO", "ADMIN"});

        JPanel p = new JPanel(new GridLayout(0, 2, 6, 6));
        p.add(new JLabel("Usuario:"));
        p.add(txtUser);
        p.add(new JLabel("Contraseña:"));
        p.add(txtPass);
        p.add(new JLabel("Nombre completo:"));
        p.add(txtNombre);
        p.add(new JLabel("Cédula:"));
        p.add(txtCedula);
        p.add(new JLabel("Rol:"));
        p.add(cmbRol);

        int op = JOptionPane.showConfirmDialog(this, p, "Nuevo usuario",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (op != JOptionPane.OK_OPTION) {
            return;
        }
        String[] datos = {txtUser.getText(), new String(txtPass.getPassword()), txtNombre.getText(),
            txtCedula.getText(), (String) cmbRol.getSelectedItem()};
        Tarea.ejecutar("USUARIO_CREAR", datos, r -> JOptionPane.showMessageDialog(this, r.getMensaje(),
                "Usuarios", r.isExito() ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE));
    }

    private void salir() {
        Conexion.getInstancia().cerrar();
        dispose();
        System.exit(0);
    }
}
