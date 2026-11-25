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
import java.util.List;

/**
 * Vista que permite filtrar los pedidos según los criterios disponibles.
 * <p>Este componente forma parte de la arquitectura modular de RegiUnic.
 */
public class FilterView extends JPanel {
    private final ViewNavigator navigator;
    private final OrderService service;
    private final JTextField txtSearch = new JTextField(18);
    private final JComboBox<String> cmbStatus = new JComboBox<>(
            new String[]{"Todos", "Pendiente", "En Proceso", "Completado", "Cancelado"});
    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"Cliente", "Producto", "Cantidad", "Total", "Estado", "Dirección"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(model);

/** Crea e inicializa este componente. */
    public FilterView(ViewNavigator navigator, OrderService service) {
        this.navigator = navigator;
        this.service = service;
        setLayout(new BorderLayout(10,10));
        setBackground(AppTheme.WINDOW_BACKGROUND);
        initUI();
    }

    private void initUI() {
        JPanel top = new JPanel(new BorderLayout(10,10));
        top.setBackground(AppTheme.PANEL_BACKGROUND);
        top.setBorder(BorderFactory.createEmptyBorder(10,15,10,15));

        AnimatedButton back = AppTheme.primaryButton("← Menú");
        back.addActionListener(e -> navigator.showMainMenu(navigator.getCurrentUser()));

        JLabel title = new JLabel("Búsquedas y Filtros", SwingConstants.CENTER);
        title.setFont(AppTheme.SECTION);
        title.setForeground(AppTheme.NAVY);
        top.add(back, BorderLayout.WEST);
        top.add(title, BorderLayout.CENTER);

        JPanel filters = new JPanel(new FlowLayout(FlowLayout.CENTER,10,5));
        filters.setOpaque(false);
        filters.add(new JLabel("Buscar cliente/producto:"));
        filters.add(txtSearch);
        filters.add(new JLabel("Estado:"));
        filters.add(cmbStatus);
        AnimatedButton search = AppTheme.primaryButton("Buscar");
        search.addActionListener(e -> applyFilter());
        filters.add(search);
        top.add(filters, BorderLayout.SOUTH);
        add(top, BorderLayout.NORTH);

        table.setFont(AppTheme.BODY);
        table.setRowHeight(29);
        table.getTableHeader().setFont(AppTheme.LABEL);
        table.getTableHeader().setBackground(AppTheme.NAVY);
        table.getTableHeader().setForeground(Color.WHITE);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder(0,16,0,16));
        ViewNavigator.configureScrollPane(scroll, table.getBackground(), false);
        add(scroll, BorderLayout.CENTER);
    }

/** Consulta y devuelve los datos correspondientes. */
    public void loadData() {
        txtSearch.setText("");
        cmbStatus.setSelectedIndex(0);
        applyFilter();
    }

    private void applyFilter() {
        AsyncExecutor.supplyAsync(
                () -> {
                    try { return service.listOrders(); }
                    catch (SQLException e) { throw new RuntimeException(e); }
                },
                orders -> {
                    String search = txtSearch.getText().trim().toLowerCase();
                    String status = (String)cmbStatus.getSelectedItem();
                    model.setRowCount(0);
                    for (Order o : orders) {
                        boolean textOk = search.isEmpty()
                                || o.client().toLowerCase().contains(search)
                                || o.product().toLowerCase().contains(search);
                        boolean statusOk = "Todos".equals(status) || o.status().equalsIgnoreCase(status);
                        if (textOk && statusOk) {
                            model.addRow(new Object[]{o.client(), o.product(), o.quantity(),
                                    String.format("$ %,.2f", o.total()), o.status(), o.address()});
                        }
                    }
                },
                error -> JOptionPane.showMessageDialog(this, "No se pudo realizar la búsqueda: " + error.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE));
    }
}
