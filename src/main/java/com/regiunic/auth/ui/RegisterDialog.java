
package com.regiunic.auth.ui;

// ============================================================
// IMPORTACIONES DEL PROYECTO
// ============================================================

import com.regiunic.auth.service.AuthService;
import com.regiunic.core.concurrent.AsyncExecutor;
import com.regiunic.core.ui.AppTheme;


// ============================================================
// IMPORTACIONES DE JAVA SWING
// ============================================================

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;


// ============================================================
// IMPORTACIONES DE JAVA AWT
// ============================================================

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;


// ============================================================
// OTRAS IMPORTACIONES
// ============================================================

import java.sql.SQLException;
import java.util.Arrays;


/**
 * ============================================================
 * CLASE RegisterDialog
 * ============================================================
 *
 * Ventana modal para registrar nuevos usuarios en RegiUnic.
 *
 * Permite:
 * - Registrar un nombre de usuario.
 * - Ingresar una contraseña.
 * - Confirmar la contraseña.
 * - Mostrar u ocultar las contraseñas.
 * - Crear una nueva cuenta.
 * - Cancelar el registro.
 *
 * Utiliza AuthService para registrar los usuarios,
 * AsyncExecutor para ejecutar operaciones en segundo plano
 * y AppTheme para conservar el diseño de RegiUnic.
 */
public class RegisterDialog extends JDialog {

    // ========================================================
    // 1. ATRIBUTOS
    // ========================================================

    // Servicio encargado de registrar los usuarios.
    private final AuthService authService;

    // Campo para escribir el nombre de usuario.
    private final JTextField txtUsuario = new JTextField(18);

    // Campo para escribir la contraseña.
    private final JPasswordField txtPassword =
            new JPasswordField(18);

    // Campo para confirmar la contraseña.
    private final JPasswordField txtConfirmacion =
            new JPasswordField(18);

    // Botón principal para crear una cuenta.
    private final JButton btnGuardar =
            AppTheme.primaryButton("Crear Cuenta");

    // Botón secundario para cancelar el registro.
    private final JButton btnCancelar =
            AppTheme.secondaryButton("Cancelar");


    // ========================================================
    // 2. CONSTRUCTOR
    // ========================================================

    /**
     * Inicializa y configura la ventana de registro.
     *
     * @param parent ventana principal de RegiUnic.
     * @param authService servicio de autenticación.
     */
    public RegisterDialog(JFrame parent, AuthService authService) {

        // Inicializar la ventana modal.
        super(parent, "RegiUnic | Registro de Cuenta", true);

        this.authService = authService;


        // ====================================================
        // CONFIGURACIÓN GENERAL DE LA VENTANA
        // ====================================================

        // Tamaño de la ventana: ancho y alto.
        setSize(500, 520);

        // Centrar la ventana respecto a su ventana principal.
        setLocationRelativeTo(parent);

        // Impedir que el usuario cambie el tamaño.
        setResizable(false);

        // Cerrar correctamente la ventana.
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // Distribución general.
        setLayout(new BorderLayout());


        // ====================================================
        // 3. PANEL PRINCIPAL
        // ====================================================

        // Contenedor que organiza todos los componentes.
        JPanel container = new JPanel(
                new BorderLayout(15, 20)
        );

        // Fondo definido en AppTheme.
        container.setBackground(AppTheme.PANEL_BACKGROUND);

        // Márgenes internos del contenedor.
        container.setBorder(
                BorderFactory.createEmptyBorder(
                        25, 30, 25, 30
                )
        );


        // ====================================================
        // 4. TÍTULO
        // ====================================================

        JLabel title = new JLabel("Registrar Nuevo Usuario");

        // Mantener la tipografía original de AppTheme.
        title.setFont(AppTheme.SECTION);

        title.setForeground(AppTheme.NAVY);

        // Centrar el título.
        title.setHorizontalAlignment(SwingConstants.CENTER);

        container.add(title, BorderLayout.NORTH);


        // ====================================================
        // 5. FORMULARIO
        // ====================================================

        JPanel form = new JPanel(
                new GridLayout(0, 1, 8, 0)
        );

        // Permitir que se vea el fondo del contenedor.
        form.setOpaque(false);


        // ====================================================
        // 6. CONFIGURACIÓN DE LOS CAMPOS DE TEXTO
        // ====================================================

        /*
         * Reducimos la altura de los campos.
         *
         * 32 = altura preferida en píxeles.
         */

        Dimension dimensionCampo = new Dimension(0, 32);

        txtUsuario.setPreferredSize(dimensionCampo);

        txtPassword.setPreferredSize(dimensionCampo);

        txtConfirmacion.setPreferredSize(dimensionCampo);


        // ====================================================
        // 7. CAMPO DE USUARIO
        // ====================================================

        JLabel lblUsuario = new JLabel("Nombre de usuario:");

        form.add(lblUsuario);

        form.add(txtUsuario);


        // ====================================================
        // 8. CAMPO DE CONTRASEÑA
        // ====================================================

        JLabel lblPassword = new JLabel(
                "Contraseña (mínimo 8 caracteres):"
        );

        form.add(lblPassword);

        form.add(txtPassword);


        // ====================================================
        // 9. CONFIRMACIÓN DE CONTRASEÑA
        // ====================================================

        JLabel lblConfirmacion = new JLabel(
                "Confirmar contraseña:"
        );

        form.add(lblConfirmacion);

        form.add(txtConfirmacion);


        // ====================================================
        // 10. BOTÓN MOSTRAR / OCULTAR CONTRASEÑA
        // ====================================================

        // Guardar los caracteres originales de ocultación.
        char echoPass = txtPassword.getEchoChar();

        char echoConf = txtConfirmacion.getEchoChar();

        // Crear el botón con el estilo secundario de RegiUnic.
        JButton btnVer = AppTheme.secondaryButton(
                "Mostrar contraseña"
        );

        // Tamaño del botón.
        btnVer.setPreferredSize(
                new Dimension(230, 40)
        );

       


        // ====================================================
        // 11. PANEL PARA CENTRAR EL BOTÓN
        // ====================================================

        JPanel panelBotonVer = new JPanel(
                new FlowLayout(FlowLayout.CENTER, 0, 3)
        );

        panelBotonVer.setOpaque(false);

        panelBotonVer.add(btnVer);


        // ====================================================
        // 12. ACCIÓN PARA MOSTRAR U OCULTAR CONTRASEÑAS
        // ====================================================

        btnVer.addActionListener(e -> {

            // Comprobar el estado actual del botón.
            boolean mostrar = btnVer.getText().equals(
                    "Mostrar contraseña"
            );

            // Mostrar u ocultar la primera contraseña.
            txtPassword.setEchoChar(
                    mostrar ? (char) 0 : echoPass
            );

            // Mostrar u ocultar la confirmación.
            txtConfirmacion.setEchoChar(
                    mostrar ? (char) 0 : echoConf
            );

            // Actualizar el texto del botón.
            btnVer.setText(
                    mostrar
                            ? "Ocultar contraseña"
                            : "Mostrar contraseña"
            );
        });


        // Agregar el botón al formulario.
        form.add(panelBotonVer);


        // ====================================================
        // 13. AGREGAR FORMULARIO AL CONTENEDOR
        // ====================================================

        container.add(form, BorderLayout.CENTER);


        // ====================================================
        // 14. PANEL DE BOTONES INFERIORES
        // ====================================================

        JPanel btnPanel = new JPanel(
                new FlowLayout(FlowLayout.CENTER, 15, 5)
        );

        btnPanel.setOpaque(false);


        // ====================================================
        // 15. TAMAÑO DE LOS BOTONES INFERIORES
        // ====================================================

        // Tamaño del botón Cancelar.
       btnCancelar.setPreferredSize(new Dimension(110, 36));
       btnCancelar.setMinimumSize(new Dimension(110, 36));;

        // Tamaño del botón Crear Cuenta.
        btnGuardar.setPreferredSize(new Dimension(150, 36));
        btnGuardar.setMinimumSize(new Dimension(150, 36));

        // Se conserva la tipografía original de ambos botones.


        // ====================================================
        // 16. ACCIÓN DEL BOTÓN CANCELAR
        // ====================================================

        // Cerrar la ventana de registro.
        btnCancelar.addActionListener(e -> dispose());


        // ====================================================
        // 17. ACCIÓN DEL BOTÓN CREAR CUENTA
        // ====================================================

        // Ejecutar el método encargado del registro.
        btnGuardar.addActionListener(
                e -> ejecutarRegistro()
        );


        // Agregar los botones al panel inferior.
        btnPanel.add(btnCancelar);

        btnPanel.add(btnGuardar);


        // Ubicar los botones en la parte inferior.
        container.add(btnPanel, BorderLayout.SOUTH);


        // ====================================================
        // 18. ESTABLECER EL CONTENIDO DE LA VENTANA
        // ====================================================

        setContentPane(container);
    }


    // ========================================================
    // 19. MÉTODO ejecutarRegistro()
    // ========================================================

    /**
     * Obtiene los datos del formulario, valida los campos
     * y solicita al servicio la creación de una cuenta.
     */
    private void ejecutarRegistro() {

        // ====================================================
        // OBTENER LOS DATOS INGRESADOS
        // ====================================================

        // Obtener el usuario y eliminar espacios externos.
        String usuario = txtUsuario.getText().trim();

        // Obtener la contraseña como arreglo de caracteres.
        char[] pass = txtPassword.getPassword();

        // Obtener la confirmación de la contraseña.
        char[] conf = txtConfirmacion.getPassword();


        // ====================================================
        // VALIDAR CAMPOS VACÍOS
        // ====================================================

        if (
                usuario.isEmpty()
                        || pass.length == 0
                        || conf.length == 0
        ) {

            JOptionPane.showMessageDialog(
                    this,
                    "Por favor completa todos los campos.",
                    "Campos vacíos",
                    JOptionPane.WARNING_MESSAGE
            );

            // Limpiar los arreglos antes de salir.
            Arrays.fill(pass, '\0');

            Arrays.fill(conf, '\0');

            return;
        }


        // ====================================================
        // DESACTIVAR EL BOTÓN DURANTE EL REGISTRO
        // ====================================================

        // Evitar múltiples solicitudes simultáneas.
        btnGuardar.setEnabled(false);

        // Informar que el registro está en proceso.
        btnGuardar.setText("Creando...");


        // ====================================================
        // EJECUTAR REGISTRO EN SEGUNDO PLANO
        // ====================================================

        AsyncExecutor.supplyAsync(

                // ------------------------------------------------
                // PROCESO DE REGISTRO
                // ------------------------------------------------

                () -> {

                    try {

                        // Enviar los datos al servicio de autenticación.
                        authService.register(
                                usuario,
                                pass,
                                conf
                        );

                        // Indicar que el proceso terminó correctamente.
                        return true;

                    } catch (SQLException ex) {

                        // Informar errores relacionados con la base de datos.
                        throw new RuntimeException(
                                "Error en la base de datos: "
                                        + ex.getMessage(),
                                ex
                        );

                    } finally {

                        // Limpiar los arreglos de contraseñas.
                        Arrays.fill(pass, '\0');

                        Arrays.fill(conf, '\0');
                    }
                },


                // ------------------------------------------------
                // SI EL REGISTRO FUE EXITOSO
                // ------------------------------------------------

                success -> {

                    JOptionPane.showMessageDialog(
                            this,
                            "Ya puedes iniciar sesion.",
                            "Registro completado",
                            JOptionPane.INFORMATION_MESSAGE
                    );

                    // Cerrar la ventana de registro.
                    dispose();
                },


                // ------------------------------------------------
                // SI OCURRE UN ERROR
                // ------------------------------------------------

                error -> {

                    // Volver a habilitar el botón.
                    btnGuardar.setEnabled(true);

                    // Restaurar su texto original.
                    btnGuardar.setText("Crear Cuenta");

                    // Obtener el mensaje de error.
                    String msg = error.getCause() != null
                            ? error.getCause().getMessage()
                            : error.getMessage();

                    // Informar al usuario del problema.
                    JOptionPane.showMessageDialog(
                            this,
                            msg,
                            "No se pudo crear la cuenta",
                            JOptionPane.WARNING_MESSAGE
                    );
                }
        );
    }
}
