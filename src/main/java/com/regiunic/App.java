package com.regiunic;

import com.regiunic.auth.repository.SqliteUserRepository;
import com.regiunic.auth.repository.UserRepository;
import com.regiunic.auth.service.AuthService;
import com.regiunic.core.config.DatabaseManager;
import com.regiunic.core.config.MigrationService;
import com.regiunic.core.ui.AppTheme;
import com.regiunic.core.ui.MainFrame;
import com.regiunic.orders.repository.OrderRepository;
import com.regiunic.orders.repository.SqliteOrderRepository;
import com.regiunic.orders.service.OrderService;
import com.regiunic.inventory.repository.ProductRepository;
import com.regiunic.inventory.repository.SqliteProductRepository;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 * Punto de entrada principal de la aplicación de escritorio RegiUnic.
 * Inicializa la base de datos SQLite, ejecuta migraciones y levanta la interfaz Swing.
 */
public class App {

/** Inicializa o prepara los recursos necesarios para el funcionamiento del módulo. */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // 1. Instalar estilos visuales compartidos
                AppTheme.install();

                // 2. Inicializar la base de datos SQLite y esquemas
                DatabaseManager.initialize();

                // 3. Inyección de dependencias de repositorios y servicios
                UserRepository userRepository = new SqliteUserRepository();
                AuthService authService = new AuthService(userRepository);

                OrderRepository orderRepository = new SqliteOrderRepository();
                ProductRepository productRepository = new SqliteProductRepository();
                OrderService orderService = new OrderService(orderRepository, productRepository);

                // 4. Migrar automáticamente datos desde CSV antiguos a SQLite si existen
                MigrationService.initializeAndMigrate(userRepository, orderRepository);

                // 5. Iniciar la ventana principal con navegación de marco único
                MainFrame mainFrame = new MainFrame(authService, orderService);
                mainFrame.setVisible(true);

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(
                        null,
                        "Error crítico al iniciar la aplicación: " + ex.getMessage(),
                        "Fallo de Inicialización",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });
    }
}
