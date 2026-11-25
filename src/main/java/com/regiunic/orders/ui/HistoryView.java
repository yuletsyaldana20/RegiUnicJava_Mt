package com.regiunic.orders.ui;

import com.regiunic.core.concurrent.AsyncExecutor;
import com.regiunic.core.ui.AppTheme;
import com.regiunic.core.ui.ViewNavigator;
import com.regiunic.core.ui.components.AnimatedButton;
import com.regiunic.orders.model.Order;
import com.regiunic.orders.service.OrderService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Pantalla para consultar y administrar el historial de pedidos.
 * <p>Este componente forma parte de la arquitectura modular de RegiUnic.
 */
public class HistoryView extends JPanel {
    private final ViewNavigator navigator;
    private final OrderService service;
    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"ID", "Cliente", "Producto", "Cantidad", "Total", "Estado", "Fecha"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(model);
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

/** Crea e inicializa este componente. */
    public HistoryView(ViewNavigator navigator, OrderService service) {
        this.navigator = navigator;
        this.service = service;
        setLayout(new BorderLayout(10,10));
        setBackground(AppTheme.WINDOW_BACKGROUND);
        initUI();
    }

    private void initUI() {
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(AppTheme.PANEL_BACKGROUND);
        top.setBorder(BorderFactory.createEmptyBorder(10,15,10,15));
        AnimatedButton back = AppTheme.primaryButton("← Menú");
        back.addActionListener(e -> navigator.showMainMenu(navigator.getCurrentUser()));
        JLabel title = new JLabel("Historial de Ventas", SwingConstants.CENTER);
        title.setFont(AppTheme.SECTION);
        title.setForeground(AppTheme.NAVY);
        top.add(back, BorderLayout.WEST);
        top.add(title, BorderLayout.CENTER);
        add(top, BorderLayout.NORTH);

        table.setFont(AppTheme.BODY);
        table.setRowHeight(29);
        table.getTableHeader().setFont(AppTheme.LABEL);
        table.getTableHeader().setBackground(AppTheme.NAVY);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(0).setPreferredWidth(0);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder(0,16,0,16));
        ViewNavigator.configureScrollPane(scroll, table.getBackground(), false);
        add(scroll, BorderLayout.CENTER);

        AnimatedButton refresh = AppTheme.secondaryButton("Actualizar historial");
        refresh.addActionListener(e -> loadData());
        AnimatedButton delete = AppTheme.dangerButton("Eliminar del historial");
        delete.addActionListener(e -> deleteSelected());
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER,10,12));
        bottom.setBackground(AppTheme.WINDOW_BACKGROUND);
        bottom.add(refresh); bottom.add(delete);
        add(bottom, BorderLayout.SOUTH);
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Selecciona un registro del historial."); return; }
        String id = String.valueOf(model.getValueAt(row, 0));
        if (JOptionPane.showConfirmDialog(this, "¿Eliminar definitivamente este registro del historial?", "Confirmar", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        AsyncExecutor.supplyAsync(() -> { try { return service.deleteHistory(id); } catch (SQLException e) { throw new RuntimeException(e); } },
                deleted -> loadData(), error -> JOptionPane.showMessageDialog(this, "No se pudo eliminar: " + error.getMessage()));
    }

/** Consulta y devuelve los datos correspondientes. */
    public void loadData() {
        AsyncExecutor.supplyAsync(
                () -> {
                    try { return service.listOrders(); }
                    catch (SQLException e) { throw new RuntimeException(e); }
                },
                orders -> {
                    model.setRowCount(0);
                    for (Order o : orders) {
                        model.addRow(new Object[]{o.id(), o.client(), o.product(), o.quantity(),
                                String.format("$ %,.2f", o.total()), o.status(),
                                o.createdAt() == null ? "" : o.createdAt().format(formatter)});
                    }
                },
                error -> JOptionPane.showMessageDialog(this, "No se pudo cargar el historial: " + error.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE));
    }
}
