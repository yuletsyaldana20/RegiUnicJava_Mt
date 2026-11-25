package com.regiunic.ui;

import com.regiunic.auth.repository.UserRepository;
import com.regiunic.auth.service.AuthService;
import com.regiunic.auth.ui.LoginView;
import com.regiunic.core.ui.AppTheme;
import com.regiunic.core.ui.MainFrame;
import com.regiunic.core.ui.ViewNavigator;
import com.regiunic.core.ui.components.AnimatedButton;
import com.regiunic.core.ui.views.PlaceholderView;
import com.regiunic.orders.repository.OrderRepository;
import com.regiunic.orders.service.OrderService;
import com.regiunic.orders.ui.MainMenuView;
import com.regiunic.orders.ui.OrdersView;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas de Sanidad y Navegación de la Interfaz de Usuario (UI)")
class UISanityTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrderRepository orderRepository;

    @BeforeAll
    static void initTheme() {
        // Instalar tema institucional
        try {
            AppTheme.install();
        } catch (Throwable ignored) {
            // En entornos CI puede no haber UI Look and Feel nativo
        }
    }

    @Test
    @DisplayName("Debe instanciar los componentes personalizados y botones con estilos corporativos")
    void testComponentCreation() {
        AnimatedButton primaryBtn = AppTheme.primaryButton("Aceptar");
        assertThat(primaryBtn).isNotNull();
        assertThat(primaryBtn.getText()).isEqualTo("Aceptar");

        AnimatedButton secondaryBtn = AppTheme.secondaryButton("Cancelar");
        assertThat(secondaryBtn).isNotNull();

        AnimatedButton dangerBtn = AppTheme.dangerButton("Eliminar");
        assertThat(dangerBtn).isNotNull();

        AnimatedButton menuBtn = AppTheme.menuButton("Opción");
        assertThat(menuBtn).isNotNull();

        JScrollPane scrollPane = new JScrollPane();
        ViewNavigator.configureScrollPane(scrollPane, Color.WHITE, false);
        assertThat(scrollPane.getVerticalScrollBar()).isNotNull();
    }

    @Test
    @DisplayName("Debe instanciar las vistas de dominio desacopladas sin errores en el Event Dispatch Thread")
    void testViewsInstantiationOnEDT() throws InterruptedException, InvocationTargetException {
        AuthService authService = new AuthService(userRepository);
        OrderService orderService = new OrderService(orderRepository);

        AtomicReference<Throwable> errorRef = new AtomicReference<>();

        SwingUtilities.invokeAndWait(() -> {
            try {
                ViewNavigator mockNavigator = new ViewNavigator() {
                    @Override public void showLogin() {}
                    @Override public void showMainMenu(String username) {}
                    @Override public void showOrders() {}
                    @Override public void showPlaceholder(String featureTitle) {}
                    @Override public String getCurrentUser() { return "usuario_test"; }
                };

                LoginView loginView = new LoginView(mockNavigator, authService);
                assertThat(loginView).isNotNull();

                MainMenuView mainMenuView = new MainMenuView(mockNavigator);
                mainMenuView.setUser("usuario_test");
                assertThat(mainMenuView).isNotNull();

                OrdersView ordersView = new OrdersView(mockNavigator, orderService);
                ordersView.setUser("usuario_test");
                assertThat(ordersView).isNotNull();

                PlaceholderView placeholderView = new PlaceholderView(mockNavigator);
                placeholderView.setFeatureTitle("Módulo Experimental");
                assertThat(placeholderView).isNotNull();

            } catch (Throwable t) {
                errorRef.set(t);
            }
        });

        assertThat(errorRef.get()).isNull();
    }

    @Test
    @DisplayName("Debe instanciar MainFrame y navegar entre vistas si no está en entorno puramente headless")
    void testMainFrameNavigation() throws InterruptedException, InvocationTargetException {
        AuthService authService = new AuthService(userRepository);
        OrderService orderService = new OrderService(orderRepository);

        AtomicReference<Throwable> errorRef = new AtomicReference<>();

        SwingUtilities.invokeAndWait(() -> {
            try {
                MainFrame frame = new MainFrame(authService, orderService);
                assertThat(frame).isNotNull();

                // Probar transiciones de navegación
                frame.showLogin();
                assertThat(frame.getCurrentUser()).isNull();

                frame.showMainMenu("gerente_test");
                assertThat(frame.getCurrentUser()).isEqualTo("gerente_test");

                frame.showOrders();
                assertThat(frame.getTitle()).contains("gerente_test");

                frame.showPlaceholder("Inventario");
                assertThat(frame.getTitle()).contains("Inventario");

                frame.dispose();
            } catch (HeadlessException e) {
                // Controlado si el entorno JVM no cuenta con pantalla física
            } catch (Throwable t) {
                errorRef.set(t);
            }
        });

        assertThat(errorRef.get()).isNull();
    }
}
