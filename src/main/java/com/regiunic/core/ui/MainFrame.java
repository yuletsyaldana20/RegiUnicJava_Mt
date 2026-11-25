package com.regiunic.core.ui;

import com.regiunic.auth.service.AuthService;
import com.regiunic.auth.ui.LoginView;
import com.regiunic.core.ui.views.PlaceholderView;
import com.regiunic.orders.service.OrderService;
import com.regiunic.orders.ui.MainMenuView;
import com.regiunic.orders.ui.OrdersView;
import com.regiunic.orders.ui.HistoryView;
import com.regiunic.orders.ui.FilterView;
import com.regiunic.orders.ui.ReportsView;
import com.regiunic.inventory.repository.ProductRepository;
import com.regiunic.inventory.repository.SqliteProductRepository;
import com.regiunic.inventory.service.ProductService;
import com.regiunic.inventory.ui.InventoryView;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.io.File;

/**
 * Ventana principal única de la aplicación de escritorio RegiUnic.
 * Gestiona el intercambio de vistas mediante CardLayout para evitar
 * el parpadeo y la sobrecarga de cerrar y crear múltiples JFrames.
 */
public class MainFrame extends JFrame implements ViewNavigator {
    private static final String VIEW_LOGIN = "LOGIN";
    private static final String VIEW_MENU = "MENU";
    private static final String VIEW_ORDERS = "ORDERS";
    private static final String VIEW_PLACEHOLDER = "PLACEHOLDER";
    private static final String VIEW_INVENTORY = "INVENTORY";
    private static final String VIEW_HISTORY = "HISTORY";
    private static final String VIEW_FILTERS = "FILTERS";
    private static final String VIEW_REPORTS = "REPORTS";

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(cardLayout);

    private final LoginView loginView;
    private final MainMenuView mainMenuView;
    private final OrdersView ordersView;
    private final PlaceholderView placeholderView;
    private final InventoryView inventoryView;
    private final HistoryView historyView;
    private final FilterView filterView;
    private final ReportsView reportsView;

    private String currentUser;

/** Crea e inicializa este componente. */
    public MainFrame(AuthService authService, OrderService orderService) {
        super("RegiUnic | Inicio de Sesión");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 650);
        setMinimumSize(new Dimension(900, 550));
        setLocationRelativeTo(null);

        // Ícono de ventana si existe
        if (new File("assets/Logo.png").exists()) {
            setIconImage(new ImageIcon("assets/Logo.png").getImage());
        }

        // Instanciar vistas desacopladas
        this.loginView = new LoginView(this, authService);
        this.mainMenuView = new MainMenuView(this);
        this.ordersView = new OrdersView(this, orderService);
        this.placeholderView = new PlaceholderView(this);
        ProductRepository productRepository = new SqliteProductRepository();
        ProductService productService = new ProductService(productRepository);
        this.inventoryView = new InventoryView(this, productService);
        this.historyView = new HistoryView(this, orderService);
        this.filterView = new FilterView(this, orderService);
        this.reportsView = new ReportsView(this, orderService);

        contentPanel.add(loginView, VIEW_LOGIN);
        contentPanel.add(mainMenuView, VIEW_MENU);
        contentPanel.add(ordersView, VIEW_ORDERS);
        contentPanel.add(placeholderView, VIEW_PLACEHOLDER);
        contentPanel.add(inventoryView, VIEW_INVENTORY);
        contentPanel.add(historyView, VIEW_HISTORY);
        contentPanel.add(filterView, VIEW_FILTERS);
        contentPanel.add(reportsView, VIEW_REPORTS);

        setContentPane(contentPanel);
        showLogin();
    }

    @Override
    public void showLogin() {
        this.currentUser = null;
        setTitle("RegiUnic | Inicio de Sesión");
        loginView.resetForm();
        cardLayout.show(contentPanel, VIEW_LOGIN);
    }

    @Override
    public void showMainMenu(String username) {
        this.currentUser = username;
        setTitle("RegiUnic | Menú Principal (" + username + ")");
        mainMenuView.setUser(username);
        cardLayout.show(contentPanel, VIEW_MENU);
    }

    @Override
    public void showOrders() {
        setTitle("RegiUnic | Pedidos y Ventas (" + currentUser + ")");
        ordersView.setUser(currentUser);
        ordersView.refreshData();
        cardLayout.show(contentPanel, VIEW_ORDERS);
    }

    @Override
    public void showInventory() {
        setTitle("RegiUnic | Gestión de Inventario (" + currentUser + ")");
        inventoryView.setUser(currentUser);
        inventoryView.refreshData();
        cardLayout.show(contentPanel, VIEW_INVENTORY);
    }

    @Override
    public void showHistory() {
        setTitle("RegiUnic | Historial de Ventas (" + currentUser + ")");
        historyView.loadData();
        cardLayout.show(contentPanel, VIEW_HISTORY);
    }

    @Override
    public void showFilters() {
        setTitle("RegiUnic | Búsquedas y Filtros (" + currentUser + ")");
        filterView.loadData();
        cardLayout.show(contentPanel, VIEW_FILTERS);
    }

    @Override
    public void showReports() {
        setTitle("RegiUnic | Reportes (" + currentUser + ")");
        reportsView.loadReport();
        cardLayout.show(contentPanel, VIEW_REPORTS);
    }

    @Override
    public void showPlaceholder(String featureTitle) {
        setTitle("RegiUnic | " + featureTitle);
        placeholderView.setFeatureTitle(featureTitle);
        cardLayout.show(contentPanel, VIEW_PLACEHOLDER);
    }

    @Override
    public String getCurrentUser() {
        return currentUser;
    }
}
