
package com.regiunic.auth.ui;

// Servicios y clases internas del proyecto.
import com.regiunic.auth.service.AuthService;
import com.regiunic.core.concurrent.AsyncExecutor;
import com.regiunic.core.ui.AppTheme;
import com.regiunic.core.ui.ViewNavigator;
import com.regiunic.core.ui.components.AnimatedButton;

// Componentes gráficos de Swing.
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

// Clases para colores, dimensiones y distribución.
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;

// Clases para la conexión y manejo de contraseñas.
import java.sql.SQLException;
import java.util.Arrays;

/**
 * Pantalla de inicio de sesión de RegiUnic.
 *
 * Esta clase utiliza JPanel para mostrar el formulario
 * dentro de una ventana principal y permitir la navegación.
 */
public class LoginView extends JPanel {

    // Permite cambiar entre las diferentes pantallas del sistema.
    private final ViewNavigator navigator;

    // Servicio encargado de autenticar a los usuarios.
    private final AuthService authService;

    // Campo donde el usuario escribe su nombre de usuario.
    private final JTextField txtUsuario = new JTextField(15);

    // Campo donde el usuario escribe su contraseña.
    private final JPasswordField txtPassword = new JPasswordField(15);

    // Botón principal para iniciar sesión.
    private final AnimatedButton btnLogin =
            AppTheme.primaryButton("Iniciar Sesión");

    // Botón secundario para abrir el registro de usuarios.
    private final AnimatedButton btnRegistrarse =
            AppTheme.secondaryButton("Registrarse");


    /**
     * Constructor de la pantalla de inicio de sesión.
     *
     * @param navigator permite navegar entre las pantallas.
     * @param authService permite validar las credenciales.
     */
    public LoginView(ViewNavigator navigator, AuthService authService) {

        // Se reciben y almacenan las dependencias.
        this.navigator = navigator;
        this.authService = authService;

        // Organiza los componentes verticalmente.
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // Establece el color de fondo de la pantalla.
        setBackground(AppTheme.WINDOW_BACKGROUND);

        // Construye todos los elementos gráficos.
        initUI();
    }


    /**
     * Construye y organiza los componentes de la interfaz.
     */
    private void initUI() {

        // Panel que permite centrar la tarjeta en la pantalla.
        JPanel centeringPanel = new JPanel(new GridBagLayout());
        centeringPanel.setBackground(AppTheme.WINDOW_BACKGROUND);

        // Tarjeta blanca que contiene el formulario.
        JPanel tarjeta = new JPanel();

        // Distribuye los componentes de la tarjeta verticalmente.
        tarjeta.setLayout(new BoxLayout(tarjeta, BoxLayout.Y_AXIS));

        // Establece el fondo blanco.
        tarjeta.setBackground(Color.WHITE);

        // Agrega un borde azul y márgenes internos.
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(20, 55, 105), 2),
                BorderFactory.createEmptyBorder(35, 45, 35, 45)
        ));


        // ---------------- LOGO INSTITUCIONAL ----------------

        // Carga el logo y ajusta sus dimensiones a 300 x 300.
        JLabel lblLogo = new JLabel(
                AppTheme.loadScaledIcon("assets/Logo.png", 300, 300)
        );

        // Centra el logo horizontalmente.
        lblLogo.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Agrega el logo a la tarjeta.
        tarjeta.add(lblLogo);

        // Agrega un espacio de 10 píxeles debajo del logo.
        tarjeta.add(Box.createRigidArea(new Dimension(0, 10)));


        // ---------------- SUBTÍTULO ----------------

        // Texto descriptivo de la aplicación.
        JLabel lblSubtitulo = new JLabel("Control de Ventas e Inventario");

        // Aplica la tipografía definida en AppTheme.
        lblSubtitulo.setFont(AppTheme.SECTION);

        // Establece el color del texto.
        lblSubtitulo.setForeground(AppTheme.TEXT_MUTED);

        // Centra el subtítulo.
        lblSubtitulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Agrega el subtítulo a la tarjeta.
        tarjeta.add(lblSubtitulo);

        // Espacio debajo del subtítulo.
        tarjeta.add(Box.createRigidArea(new Dimension(0, 20)));


        // ---------------- CAMPO USUARIO ----------------

        // Etiqueta del campo de usuario.
        JLabel lblUsuario = new JLabel("Usuario");

        // Aplica el estilo de los títulos de formulario.
        lblUsuario.setFont(AppTheme.FORM_HEADING);

        // Establece el color azul.
        lblUsuario.setForeground(AppTheme.NAVY);

        // Centra la etiqueta.
        lblUsuario.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Agrega la etiqueta.
        tarjeta.add(lblUsuario);

        // Espacio entre la etiqueta y el campo.
        tarjeta.add(Box.createRigidArea(new Dimension(0, 5)));

        // Establece el tamaño máximo del campo de usuario.
        txtUsuario.setMaximumSize(new Dimension(260, 36));

        // Centra el campo.
        txtUsuario.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Agrega el campo a la tarjeta.
        tarjeta.add(txtUsuario);

        // Espacio debajo del campo.
        tarjeta.add(Box.createRigidArea(new Dimension(0, 15)));


        // ---------------- CAMPO CONTRASEÑA ----------------

        // Etiqueta del campo de contraseña.
        JLabel lblPassword = new JLabel("Contraseña");

        // Aplica la tipografía del tema.
        lblPassword.setFont(AppTheme.FORM_HEADING);

        // Establece el color del texto.
        lblPassword.setForeground(AppTheme.NAVY);

        // Centra la etiqueta.
        lblPassword.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Agrega la etiqueta.
        tarjeta.add(lblPassword);

        // Espacio entre la etiqueta y el campo.
        tarjeta.add(Box.createRigidArea(new Dimension(0, 5)));

        // Establece el tamaño máximo del campo de contraseña.
        txtPassword.setMaximumSize(new Dimension(260, 36));

        // Centra el campo.
        txtPassword.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Agrega el campo.
        tarjeta.add(txtPassword);

        // Espacio debajo del campo.
        tarjeta.add(Box.createRigidArea(new Dimension(0, 8)));


        // ---------------- MOSTRAR CONTRASEÑA ----------------

        // Casilla para mostrar u ocultar la contraseña.
        JCheckBox chkMostrar = new JCheckBox("Mostrar contraseña");

        // Aplica la tipografía del tema.
        chkMostrar.setFont(AppTheme.BODY);

        // Establece el color del texto.
        chkMostrar.setForeground(AppTheme.TEXT_MUTED);

        // Mantiene el fondo blanco.
        chkMostrar.setBackground(Color.WHITE);

        // Elimina el efecto visual del foco.
        chkMostrar.setFocusPainted(false);

        // Centra la casilla.
        chkMostrar.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Cambia la visibilidad de la contraseña al seleccionar la casilla.
        chkMostrar.addActionListener(e -> {
            txtPassword.setEchoChar(
                    chkMostrar.isSelected() ? (char) 0 : '•'
            );
        });

        // Agrega la casilla a la tarjeta.
        tarjeta.add(chkMostrar);

        // Espacio antes de los botones.
        tarjeta.add(Box.createRigidArea(new Dimension(0, 20)));


        // ---------------- BOTONES DE ACCIÓN ----------------

        // Mantiene la tipografía definida para las acciones.
        btnLogin.setFont(AppTheme.ACTION);

        // TAMAÑO DEL BOTÓN INICIAR SESIÓN.
        
        btnLogin.setPreferredSize(new Dimension(200, 60));
        btnLogin.setMinimumSize(new Dimension(200, 60));

        // Ejecuta el proceso de autenticación al hacer clic.
        btnLogin.addActionListener(e -> ejecutarLogin());

        // Define las dimensiones del botón Registrarse.
        btnRegistrarse.setPreferredSize(new Dimension(200, 60));
        btnRegistrarse.setMinimumSize(new Dimension(200, 60));

        // Abre la ventana de registro.
        btnRegistrarse.addActionListener(e -> {

            // Obtiene la ventana principal que contiene esta pantalla.
            JFrame parentFrame =
                    (JFrame) SwingUtilities.getWindowAncestor(this);

            // Crea y muestra el diálogo de registro.
            new RegisterDialog(parentFrame, authService).setVisible(true);
        });


        // ---------------- ATAJOS CON ENTER ----------------

        // Al presionar Enter en contraseña, inicia sesión.
        txtPassword.addActionListener(e -> ejecutarLogin());

        // Al presionar Enter en usuario, pasa al campo contraseña.
        txtUsuario.addActionListener(e ->
                txtPassword.requestFocusInWindow()
        );


        // ---------------- PANEL DE BOTONES ----------------

        // Organiza los botones horizontalmente y los centra.
        JPanel panelBotones =
                new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));

        // Establece el fondo blanco.
        panelBotones.setBackground(Color.WHITE);

        // Centra el panel dentro de la tarjeta.
        panelBotones.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Agrega el botón de inicio de sesión.
        panelBotones.add(btnLogin);

        // Agrega el botón de registro.
        panelBotones.add(btnRegistrarse);

        // Incorpora el panel de botones a la tarjeta.
        tarjeta.add(panelBotones);


        // ---------------- PANEL CENTRAL ----------------

        // Agrega la tarjeta al panel que la centra.
        centeringPanel.add(tarjeta);

        // Permite desplazarse si la pantalla no tiene suficiente espacio.
        JScrollPane scrollPane = new JScrollPane(centeringPanel);

        // Elimina el borde del área desplazable.
        scrollPane.setBorder(null);

        // Configura los colores y el comportamiento del desplazamiento.
        ViewNavigator.configureScrollPane(
                scrollPane,
                AppTheme.WINDOW_BACKGROUND,
                false
        );

        // Agrega el área desplazable a la pantalla.
        add(scrollPane);
    }


    /**
     * Ejecuta el proceso de autenticación del usuario.
     */
    private void ejecutarLogin() {

        // Obtiene el usuario y elimina espacios innecesarios.
        String usuario = txtUsuario.getText().trim();

        // Obtiene la contraseña como arreglo de caracteres.
        char[] password = txtPassword.getPassword();

        // Comprueba que ambos campos tengan información.
        if (usuario.isEmpty() || password.length == 0) {

            // Limpia la contraseña de la memoria.
            Arrays.fill(password, '\0');

            // Informa al usuario que faltan datos.
            JOptionPane.showMessageDialog(
                    this,
                    "Por favor, ingrese el usuario y la contraseña.",
                    "Campos incompletos",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        // Deshabilita los controles mientras se valida el acceso.
        setControlesHabilitados(false);

        // Cambia temporalmente el texto del botón.
        btnLogin.setText("Verificando...");


        // Ejecuta la autenticación en segundo plano.
        AsyncExecutor.supplyAsync(

                // Tarea que se ejecuta fuera del hilo de la interfaz.
                () -> {
                    try {

                        // Comprueba las credenciales mediante AuthService.
                        return authService.authenticate(usuario, password);

                    } catch (SQLException ex) {

                        // Convierte el error SQL en una excepción de ejecución.
                        throw new RuntimeException(
                                "Error al conectar con la base de datos SQLite: "
                                        + ex.getMessage(),
                                ex
                        );

                    } finally {

                        // Limpia la contraseña de la memoria al terminar.
                        Arrays.fill(password, '\0');
                    }
                },

                // Se ejecuta cuando la autenticación termina correctamente.
                autenticado -> {

                    // Vuelve a habilitar los controles.
                    setControlesHabilitados(true);

                    // Restaura el texto del botón.
                    btnLogin.setText("Iniciar Sesión");

                    // Comprueba si las credenciales son correctas.
                    if (autenticado) {

                        // Limpia el campo de contraseña.
                        txtPassword.setText("");

                        // Navega al menú principal.
                        navigator.showMainMenu(usuario);

                    } else {

                        // Informa que las credenciales no coinciden.
                        JOptionPane.showMessageDialog(
                                this,
                                "El usuario o la contraseña no son correctos.",
                                "No se pudo iniciar sesión",
                                JOptionPane.WARNING_MESSAGE
                        );
                    }
                },

                // Se ejecuta si ocurre un error durante la autenticación.
                error -> {

                    // Habilita nuevamente los controles.
                    setControlesHabilitados(true);

                    // Restaura el texto del botón.
                    btnLogin.setText("Iniciar Sesión");

                    // Muestra el mensaje del error.
                    JOptionPane.showMessageDialog(
                            this,
                            "Error durante la autenticación: "
                                    + error.getMessage(),
                            "Error de Acceso",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
        );
    }


    /**
     * Habilita o deshabilita los controles del formulario.
     *
     * @param habilitar true para habilitar y false para deshabilitar.
     */
    private void setControlesHabilitados(boolean habilitar) {

        // Habilita o deshabilita el botón de inicio de sesión.
        btnLogin.setEnabled(habilitar);

        // Habilita o deshabilita el botón de registro.
        btnRegistrarse.setEnabled(habilitar);

        // Habilita o deshabilita el campo de usuario.
        txtUsuario.setEnabled(habilitar);

        // Habilita o deshabilita el campo de contraseña.
        txtPassword.setEnabled(habilitar);
    }


    /**
     * Limpia el formulario y restaura los controles.
     * Se puede utilizar al regresar a la pantalla de inicio.
     */
    public void resetForm() {

        // Borra el nombre de usuario.
        txtUsuario.setText("");

        // Borra la contraseña.
        txtPassword.setText("");

        // Habilita nuevamente todos los controles.
        setControlesHabilitados(true);

        // Restaura el texto original del botón.
        btnLogin.setText("Iniciar Sesión");
    }
}
