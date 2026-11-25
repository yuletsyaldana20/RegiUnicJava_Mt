package com.regiunic.inventory.ui;

import com.regiunic.core.concurrent.AsyncExecutor;
import com.regiunic.core.ui.AppTheme;
import com.regiunic.core.ui.ViewNavigator;
import com.regiunic.core.ui.components.AnimatedButton;
import com.regiunic.inventory.model.Product;
import com.regiunic.inventory.service.ProductService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/**
 * Pantalla Swing para consultar y gestionar el inventario.
 * <p>Este componente forma parte de la arquitectura modular de RegiUnic.
 */
public class InventoryView extends JPanel {
    private final ViewNavigator navigator;
    private final ProductService service;
    private final JLabel userLabel = new JLabel();
    private final AnimatedButton btnRefresh = AppTheme.secondaryButton("Actualizar");
    private final AnimatedButton btnAdd = AppTheme.primaryButton("Agregar producto");

    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"ID", "Producto", "Stock", "Precio", "Categoría"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable table = new JTable(model);

/** Crea e inicializa este componente. */
    public InventoryView(ViewNavigator navigator, ProductService service) {
        this.navigator = navigator;
        this.service = service;
        setLayout(new BorderLayout(10, 10));
        setBackground(AppTheme.WINDOW_BACKGROUND);
        initUI();
    }

    private void initUI() {
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(AppTheme.PANEL_BACKGROUND);
        top.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        AnimatedButton back = AppTheme.primaryButton("← Menú");
        back.addActionListener(e -> navigator.showMainMenu(navigator.getCurrentUser()));

        JLabel title = new JLabel("Gestión de Inventario", SwingConstants.CENTER);
        title.setFont(AppTheme.SECTION);
        title.setForeground(AppTheme.NAVY);

        userLabel.setFont(AppTheme.LABEL);
        userLabel.setForeground(AppTheme.TEXT_MUTED);
        userLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        top.add(back, BorderLayout.WEST);
        top.add(title, BorderLayout.CENTER);
        top.add(userLabel, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        table.setFont(AppTheme.BODY);
        table.setRowHeight(30);
        table.getTableHeader().setFont(AppTheme.LABEL);
        table.getTableHeader().setBackground(AppTheme.NAVY);
        table.getTableHeader().setForeground(Color.WHITE);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));
        ViewNavigator.configureScrollPane(scroll, table.getBackground(), false);
        add(scroll, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 12));
        bottom.setBackground(AppTheme.WINDOW_BACKGROUND);
        btnAdd.setPreferredSize(new Dimension(190, 44));
        btnRefresh.setPreferredSize(new Dimension(150, 44));
        AnimatedButton edit = AppTheme.secondaryButton("Editar");
        AnimatedButton delete = AppTheme.dangerButton("Eliminar");
        edit.setPreferredSize(new Dimension(130, 44));
        delete.setPreferredSize(new Dimension(130, 44));

        btnAdd.addActionListener(e -> showProductDialog(null));
        edit.addActionListener(e -> editSelected());
        delete.addActionListener(e -> deleteSelected());
        btnRefresh.addActionListener(e -> refreshData());

        bottom.add(btnAdd);
        bottom.add(edit);
        bottom.add(delete);
        bottom.add(btnRefresh);
        add(bottom, BorderLayout.SOUTH);
    }

/** Actualiza los datos indicados después de validar la información necesaria. */
    public void setUser(String username) {
        userLabel.setText("Usuario: " + (username == null ? "" : username));
    }

/** Ejecuta la operación indicada por este método. */
    public void refreshData() {
        btnRefresh.setEnabled(false);
        AsyncExecutor.supplyAsync(
                () -> {
                    try { return service.listProducts(); }
                    catch (SQLException e) { throw new RuntimeException(e); }
                },
                products -> {
                    model.setRowCount(0);
                    for (Product p : products) {
                        model.addRow(new Object[]{p.id(), p.name(), p.stock(),
                                String.format("$ %,.2f", p.price()), p.category()});
                    }
                    btnRefresh.setEnabled(true);
                },
                error -> {
                    btnRefresh.setEnabled(true);
                    JOptionPane.showMessageDialog(this, "No se pudo cargar el inventario: " + error.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                });
    }

    private void showProductDialog(Product product) {
        JTextField name = new JTextField(product == null ? "" : product.name());
        JTextField stock = new JTextField(product == null ? "0" : String.valueOf(product.stock()));
        JTextField price = new JTextField(product == null ? "" : String.valueOf(product.price()));
        JTextField category = new JTextField(product == null ? "" : product.category());

        Object[] fields = {"Producto:", name, "Stock:", stock, "Precio:", price, "Categoría:", category};
        int option = JOptionPane.showConfirmDialog(this, fields,
                product == null ? "Agregar producto" : "Editar producto",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (option != JOptionPane.OK_OPTION) return;

        btnAdd.setEnabled(false);
        AsyncExecutor.supplyAsync(
                () -> {
                    try {
                        if (product == null) service.save(name.getText(), stock.getText(), price.getText(), category.getText());
                        else service.update(product.id(), name.getText(), stock.getText(), price.getText(), category.getText());
                        return true;
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                },
                ok -> { btnAdd.setEnabled(true); refreshData(); },
                error -> {
                    btnAdd.setEnabled(true);
                    JOptionPane.showMessageDialog(this, cleanError(error), "Revisa los datos", JOptionPane.WARNING_MESSAGE);
                });
    }

    private void editSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona un producto.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int id = (int) model.getValueAt(row, 0);
        try {
            Product product = service.listProducts().stream().filter(p -> p.id() == id).findFirst().orElse(null);
            if (product != null) showProductDialog(product);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "No se pudo consultar el producto.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona un producto.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int id = (int) model.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "¿Eliminar este producto?",
                "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        AsyncExecutor.supplyAsync(
                () -> {
                    try { return service.delete(id); }
                    catch (SQLException e) { throw new RuntimeException(e); }
                },
                deleted -> refreshData(),
                error -> JOptionPane.showMessageDialog(this, "No se pudo eliminar: " + cleanError(error),
                        "Error", JOptionPane.ERROR_MESSAGE));
    }

    private String cleanError(Throwable error) {
        return error.getCause() != null ? error.getCause().getMessage() : error.getMessage();
    }
}
