package com.regiunic.core.ui;

import com.regiunic.core.ui.components.ModernScrollBarUI;

import javax.swing.JScrollPane;
import java.awt.Color;

/**
 * Interfaz para la navegación entre vistas dentro del marco principal de escritorio.
 */
public interface ViewNavigator {
    void showLogin();
    void showMainMenu(String username);
    void showOrders();
    default void showInventory() { showPlaceholder("Gestión de Inventario"); }
    default void showHistory() { showPlaceholder("Historial de Ventas"); }
    default void showFilters() { showPlaceholder("Búsquedas y Filtros"); }
    default void showReports() { showPlaceholder("Reportes"); }
    void showPlaceholder(String featureTitle);
    String getCurrentUser();

    static void configureScrollPane(JScrollPane panel, Color backgroundColor, boolean allowHorizontal) {
        panel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        panel.setHorizontalScrollBarPolicy(allowHorizontal
                ? JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
                : JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        panel.getViewport().setBackground(backgroundColor);
        panel.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        panel.getHorizontalScrollBar().setUI(new ModernScrollBarUI());
        panel.getVerticalScrollBar().setUnitIncrement(18);
        panel.getHorizontalScrollBar().setUnitIncrement(18);
    }
}
