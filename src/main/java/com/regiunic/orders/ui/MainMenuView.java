package com.regiunic.orders.ui;

import com.regiunic.core.ui.AppTheme;
import com.regiunic.core.ui.ViewNavigator;
import com.regiunic.core.ui.components.AnimatedButton;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;

/**
 * Vista del Menú Principal del sistema RegiUnic.
 */
public class MainMenuView extends JPanel {
    private final ViewNavigator navigator;
    private final JLabel lblUsuarioLogueado;

/** Crea e inicializa este componente. */
    public MainMenuView(ViewNavigator navigator) {
        this.navigator = navigator;
        setLayout(new BorderLayout());
        setBackground(AppTheme.WINDOW_BACKGROUND);

        // Barra superior institucional
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(AppTheme.NAVY);
        topBar.setPreferredSize(new Dimension(1100, 52));
        topBar.setBorder(BorderFactory.createEmptyBorder(4, 15, 4, 15));

        JLabel lblLogo = new JLabel(AppTheme.loadScaledIcon("assets/Logo.png", 65, 44));
        topBar.add(lblLogo, BorderLayout.WEST);

        lblUsuarioLogueado = new JLabel("Usuario: ");
        lblUsuarioLogueado.setFont(AppTheme.LABEL);
        lblUsuarioLogueado.setForeground(Color.WHITE);
        topBar.add(lblUsuarioLogueado, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // Panel central
        JPanel panelCentral = new JPanel();
        panelCentral.setLayout(new BoxLayout(panelCentral, BoxLayout.Y_AXIS));
        panelCentral.setBackground(AppTheme.WINDOW_BACKGROUND);
        panelCentral.setBorder(BorderFactory.createEmptyBorder(35, 40, 35, 40));

        JLabel lblTituloMenu = new JLabel("Menú de Control");
        lblTituloMenu.setFont(AppTheme.PAGE_TITLE);
        lblTituloMenu.setForeground(AppTheme.NAVY);
        lblTituloMenu.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelCentral.add(lblTituloMenu);

        panelCentral.add(Box.createRigidArea(new Dimension(0, 35)));

        JPanel gridBotones = new JPanel(new GridLayout(2, 3, 20, 20));
        gridBotones.setBackground(AppTheme.WINDOW_BACKGROUND);
        gridBotones.setMaximumSize(new Dimension(880, 240));

        AnimatedButton btnInventario = AppTheme.menuButton("<html><center>Gestión de<br>Inventario</center></html>");
        AnimatedButton btnPedidos = AppTheme.menuButton("<html><center>Registrar<br>Pedido / Venta</center></html>");
        AnimatedButton btnHistorial = AppTheme.menuButton("<html><center>Historial<br>de Ventas</center></html>");
        AnimatedButton btnFiltros = AppTheme.menuButton("<html><center>Búsquedas<br>y Filtros</center></html>");
        AnimatedButton btnReportes = AppTheme.menuButton("<html><center>Reportes</center></html>");
        AnimatedButton btnLogout = AppTheme.dangerButton("<html><center>Cerrar<br>Sesión</center></html>");

        btnInventario.setFont(AppTheme.MENU_ITEM);
        btnPedidos.setFont(AppTheme.MENU_ITEM);
        btnHistorial.setFont(AppTheme.MENU_ITEM);
        btnFiltros.setFont(AppTheme.MENU_ITEM);
        btnReportes.setFont(AppTheme.MENU_ITEM);
        btnLogout.setFont(AppTheme.MENU_ITEM);

        btnInventario.addActionListener(e -> navigator.showInventory());
        btnPedidos.addActionListener(e -> navigator.showOrders());
        btnHistorial.addActionListener(e -> navigator.showHistory());
        btnFiltros.addActionListener(e -> navigator.showFilters());
        btnReportes.addActionListener(e -> navigator.showReports());

        btnLogout.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "¿Deseas cerrar la sesión actual?",
                    "Cerrar Sesión",
                    JOptionPane.YES_NO_OPTION
            );
            if (confirm == JOptionPane.YES_OPTION) {
                navigator.showLogin();
            }
        });

        gridBotones.add(btnInventario);
        gridBotones.add(btnPedidos);
        gridBotones.add(btnHistorial);
        gridBotones.add(btnFiltros);
        gridBotones.add(btnReportes);
        gridBotones.add(btnLogout);

        panelCentral.add(gridBotones);

        JScrollPane menuScroll = new JScrollPane(panelCentral);
        menuScroll.setBorder(null);
        ViewNavigator.configureScrollPane(menuScroll, panelCentral.getBackground(), false);
        add(menuScroll, BorderLayout.CENTER);
    }

/** Actualiza los datos indicados después de validar la información necesaria. */
    public void setUser(String username) {
        lblUsuarioLogueado.setText("Usuario: " + (username == null ? "" : username));
    }
}
