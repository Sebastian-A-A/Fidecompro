package fidecompro.cliente;

import fidecompro.comun.Respuesta;
import fidecompro.comun.Usuario;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

/** Pantalla de ingreso con usuario y contraseña */
public class VentanaLogin extends JFrame {

    private final JTextField txtHost = new JTextField("localhost", 14);
    private final JTextField txtPuerto = new JTextField("5000", 6);
    private final JTextField txtUsuario = new JTextField(14);
    private final JPasswordField txtPassword = new JPasswordField(14);
    private final JButton btnIngresar = new JButton("Ingresar");
    private final JButton btnSalir = new JButton("Salir");
    private final JLabel lblEstado = new JLabel(" ", SwingConstants.CENTER);

    public VentanaLogin() {
        super("Fidecompro - Iniciar sesión");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JLabel titulo = new JLabel("Fidecompro - Facturación", SwingConstants.CENTER);
        titulo.setFont(titulo.getFont().deriveFont(java.awt.Font.BOLD, 18f));
        titulo.setBorder(new EmptyBorder(14, 10, 4, 10));

        JPanel form = new JPanel(new GridBagLayout());
        campo(form, "Servidor:", txtHost, 0);
        campo(form, "Puerto:", txtPuerto, 1);
        campo(form, "Usuario:", txtUsuario, 2);
        campo(form, "Contraseña:", txtPassword, 3);

        lblEstado.setForeground(Color.RED);
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 6));
        botones.add(btnIngresar);
        botones.add(btnSalir);
        JPanel sur = new JPanel(new BorderLayout());
        sur.add(lblEstado, BorderLayout.NORTH);
        sur.add(botones, BorderLayout.CENTER);

        add(titulo, BorderLayout.NORTH);
        add(form, BorderLayout.CENTER);
        add(sur, BorderLayout.SOUTH);

        btnIngresar.addActionListener(e -> ingresar());
        btnSalir.addActionListener(e -> System.exit(0));
        txtPassword.addActionListener(e -> ingresar());
        getRootPane().setDefaultButton(btnIngresar);

        pack();
        setMinimumSize(getSize());
        setLocationRelativeTo(null);
    }

    private static void campo(JPanel p, String texto, JComponent c, int fila) {
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 8, 4, 8);
        g.gridy = fila;
        g.anchor = GridBagConstraints.WEST;
        g.gridx = 0;
        p.add(new JLabel(texto), g);
        g.gridx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;
        p.add(c, g);
    }

    private void ingresar() {
        final String host = txtHost.getText().trim();
        final String usuario = txtUsuario.getText().trim();
        final String password = new String(txtPassword.getPassword());
        final int puerto;
        try {
            puerto = Integer.parseInt(txtPuerto.getText().trim());
        } catch (NumberFormatException ex) {
            lblEstado.setText("El puerto debe ser un número.");
            return;
        }
        if (usuario.isEmpty() || password.isEmpty()) {
            lblEstado.setText("Ingrese usuario y contraseña.");
            return;
        }

        btnIngresar.setEnabled(false);
        lblEstado.setForeground(Color.DARK_GRAY);
        lblEstado.setText("Conectando...");

        new SwingWorker<Respuesta, Void>() {
            @Override
            protected Respuesta doInBackground() {
                try {
                    Conexion.getInstancia().conectar(host, puerto);
                } catch (IOException e) {
                    return new Respuesta(false, "No se pudo conectar al servidor: " + e.getMessage(), null);
                }
                return Conexion.getInstancia().enviar("LOGIN", new String[]{usuario, password});
            }

            @Override
            protected void done() {
                btnIngresar.setEnabled(true);
                try {
                    Respuesta r = get();
                    if (r.isExito()) {
                        new VentanaPrincipal((Usuario) r.getDatos()).setVisible(true);
                        dispose();
                    } else {
                        lblEstado.setForeground(Color.RED);
                        lblEstado.setText(r.getMensaje());
                        txtPassword.setText("");
                    }
                } catch (Exception e) {
                    lblEstado.setForeground(Color.RED);
                    lblEstado.setText("Error: " + e.getMessage());
                }
            }
        }.execute();
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignorado) {
        }
        SwingUtilities.invokeLater(() -> new VentanaLogin().setVisible(true));
    }
}
