package com.regiunic.orders.ui;

import com.regiunic.core.concurrent.AsyncExecutor;
import com.regiunic.core.ui.AppTheme;
import com.regiunic.core.ui.ViewNavigator;
import com.regiunic.core.ui.components.AnimatedButton;
import com.regiunic.orders.model.Order;
import com.regiunic.orders.service.OrderService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Vista de gestión y registro de pedidos/ventas.
 */
public class OrdersView extends JPanel {
    private final ViewNavigator navigator;
    private final OrderService orderService;
    private final JLabel lblUsuarioLogueado = new JLabel("");
    private final AnimatedButton btnRefresh = AppTheme.secondaryButton("Actualizar lista");
    private final AnimatedButton btnAdd = AppTheme.primaryButton("Registrar pedido");
    private final AnimatedButton btnEdit = AppTheme.secondaryButton("Editar pedido");
    private final AnimatedButton btnDelete = AppTheme.dangerButton("Eliminar pedido");

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"ID", "Cliente", "Producto", "Cant.", "Precio Unit.", "Total", "Dirección", "Estado", "Fecha"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(tableModel);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

/** Crea e inicializa este componente. */
    public OrdersView(ViewNavigator navigator, OrderService orderService) {
        this.navigator = navigator;
        this.orderService = orderService;

        setLayout(new BorderLayout(10, 10));
        setBackground(AppTheme.WINDOW_BACKGROUND);

        initUI();
    }

    private void initUI() {
        // Cabecera superior
        JPanel cabecera = new JPanel(new GridLayout(1, 3));
        cabecera.setBackground(AppTheme.PANEL_BACKGROUND);
        cabecera.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));

        AnimatedButton btnVolver = AppTheme.primaryButton("← Menú principal");
        btnVolver.addActionListener(e -> navigator.showMainMenu(navigator.getCurrentUser()));

        JPanel zonaVolver = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        zonaVolver.setOpaque(false);
        zonaVolver.add(btnVolver);

        JLabel title = new JLabel("RegiUnic — Control de Pedidos", SwingConstants.CENTER);
        title.setFont(AppTheme.SECTION);
        title.setForeground(AppTheme.NAVY);

        lblUsuarioLogueado.setFont(AppTheme.LABEL);
        lblUsuarioLogueado.setForeground(AppTheme.TEXT_MUTED);
        lblUsuarioLogueado.setHorizontalAlignment(SwingConstants.RIGHT);

        cabecera.add(zonaVolver);
        cabecera.add(title);
        cabecera.add(lblUsuarioLogueado);
        add(cabecera, BorderLayout.NORTH);

        // Tabla de datos
        table.setFont(AppTheme.BODY);
        table.setRowHeight(30);
        table.getTableHeader().setFont(AppTheme.LABEL);
        table.getTableHeader().setBackground(AppTheme.NAVY);
        table.getTableHeader().setForeground(Color.WHITE);
        table.setSelectionBackground(AppTheme.SURFACE_HOVER);
        table.setSelectionForeground(AppTheme.NAVY);
        table.setShowGrid(true);
        table.setGridColor(new Color(230, 235, 245));
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);

        // Alineación centrada para columnas clave
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(3).setCellRenderer(centerRenderer); // Cantidad
        table.getColumnModel().getColumn(7).setCellRenderer(centerRenderer); // Estado
        table.getColumnModel().getColumn(8).setCellRenderer(centerRenderer); // Fecha

        // Alineación derecha para precios
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        table.getColumnModel().getColumn(4).setCellRenderer(rightRenderer); // Precio
        table.getColumnModel().getColumn(5).setCellRenderer(rightRenderer); // Total

        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));
        ViewNavigator.configureScrollPane(tableScroll, table.getBackground(), true);
        add(tableScroll, BorderLayout.CENTER);

        // Barra inferior de botones
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        buttonsPanel.setBackground(AppTheme.WINDOW_BACKGROUND);

        // Tamaño de los botones.
        btnAdd.setPreferredSize(new Dimension(190, 44));
        btnEdit.setPreferredSize(new Dimension(190, 44));
        btnDelete.setPreferredSize(new Dimension(190, 44));
        btnRefresh.setPreferredSize(new Dimension(180, 44));

        btnAdd.addActionListener(e -> mostrarDialogoRegistro());
        btnEdit.addActionListener(e -> editarSeleccionado());
        btnDelete.addActionListener(e -> eliminarSeleccionado());
        btnRefresh.addActionListener(e -> refreshData());

        buttonsPanel.add(btnAdd);
        buttonsPanel.add(btnEdit);
        buttonsPanel.add(btnDelete);
        buttonsPanel.add(btnRefresh);
        add(buttonsPanel, BorderLayout.SOUTH);
    }

/** Actualiza los datos indicados después de validar la información necesaria. */
    public void setUser(String username) {
        lblUsuarioLogueado.setText("Usuario: " + (username == null ? "" : username));
    }

/** Ejecuta la operación indicada por este método. */
    public void refreshData() {
        btnRefresh.setEnabled(false);
        btnRefresh.setText("Cargando...");

        AsyncExecutor.supplyAsync(
                () -> {
                    try {
                        return orderService.listActiveOrders();
                    } catch (SQLException ex) {
                        throw new RuntimeException("Error al consultar pedido: " + ex.getMessage(), ex);
                    }
                },
                orders -> {
                    tableModel.setRowCount(0);
                    for (Order o : orders) {
                        String fechaFormateada = o.createdAt() != null
                                ? o.createdAt().format(DATE_FORMATTER)
                                : "";
                        tableModel.addRow(new Object[]{
                                o.id(),
                                o.client(),
                                o.product(),
                                o.quantity(),
                                String.format("$ %,.2f", o.unitPrice()),
                                String.format("$ %,.2f", o.total()),
                                o.address(),
                                o.status(),
                                fechaFormateada
                        });
                    }
                    btnRefresh.setEnabled(true);
                    btnRefresh.setText("Actualizar lista");
                },
                error -> {
                    btnRefresh.setEnabled(true);
                    btnRefresh.setText("Actualizar lista");
                    JOptionPane.showMessageDialog(
                            this,
                            "No se pudieron cargar los pedidos: " + error.getMessage(),
                            "Error de Datos",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
        );
    }


    private void editarSeleccionado() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona un pedido.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String id = String.valueOf(tableModel.getValueAt(row, 0));
        try {
            Order order = orderService.getOrder(id).orElse(null);
            if (order == null) {
                JOptionPane.showMessageDialog(this, "No se encontró el pedido.", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }

            JTextField client = new JTextField(order.client());
            JTextField product = new JTextField(order.product());
            JTextField quantity = new JTextField(String.valueOf(order.quantity()));
            JTextField price = new JTextField(String.valueOf(order.unitPrice()));
            JTextField address = new JTextField(order.address());
            JComboBox<String> status = new JComboBox<>(OrderService.VALID_STATUSES.toArray(new String[0]));
            status.setSelectedItem(order.status());

            Object[] fields = {
                    "Cliente:", client,
                    "Producto:", product,
                    "Cantidad:", quantity,
                    "Precio unitario:", price,
                    "Dirección:", address,
                    "Estado:", status
            };

            int option = JOptionPane.showConfirmDialog(this, fields,
                    "Editar Pedido", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (option != JOptionPane.OK_OPTION) return;

            btnEdit.setEnabled(false);
            AsyncExecutor.supplyAsync(
                    () -> {
                        try {
                            orderService.updateOrder(id, client.getText(), product.getText(),
                                    quantity.getText(), price.getText(), address.getText(),
                                    String.valueOf(status.getSelectedItem()));
                            return true;
                        } catch (SQLException ex) {
                            throw new RuntimeException("No se pudo actualizar el pedido: " + ex.getMessage(), ex);
                        }
                    },
                    ok -> {
                        btnEdit.setEnabled(true);
                        refreshData();
                    },
                    error -> {
                        btnEdit.setEnabled(true);
                        JOptionPane.showMessageDialog(this, cleanError(error),
                                "Revisa los datos", JOptionPane.WARNING_MESSAGE);
                    });
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "No se pudo consultar el pedido: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void eliminarSeleccionado() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona un pedido.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String id = String.valueOf(tableModel.getValueAt(row, 0));
        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Deseas eliminar el pedido seleccionado?",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) return;

        btnDelete.setEnabled(false);
        AsyncExecutor.supplyAsync(
                () -> {
                    try {
                        return orderService.deleteOrder(id);
                    } catch (SQLException ex) {
                        throw new RuntimeException("No se pudo eliminar el pedido: " + ex.getMessage(), ex);
                    }
                },
                deleted -> {
                    btnDelete.setEnabled(true);
                    refreshData();
                    if (!deleted) {
                        JOptionPane.showMessageDialog(this, "El pedido ya no existe.", "Aviso", JOptionPane.WARNING_MESSAGE);
                    }
                },
                error -> {
                    btnDelete.setEnabled(true);
                    JOptionPane.showMessageDialog(this, cleanError(error), "Error", JOptionPane.ERROR_MESSAGE);
                });
    }

    private String cleanError(Throwable error) {
        return error.getCause() != null ? error.getCause().getMessage() : error.getMessage();
    }

    private void mostrarDialogoRegistro() {
        JTextField txtClient = new JTextField();
        JTextField txtProduct = new JTextField();
        JTextField txtQuantity = new JTextField("1");
        JTextField txtPrice = new JTextField();
        JTextField txtAddress = new JTextField();

        Object[] fields = {
                "Nombre del Cliente:", txtClient,
                "Producto:", txtProduct,
                "Cantidad:", txtQuantity,
                "Precio Unitario ($):", txtPrice,
                "Dirección de Entrega:", txtAddress
        };

        int option = JOptionPane.showConfirmDialog(
                this,
                fields,
                "Registrar Nuevo Pedido / Venta",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (option != JOptionPane.OK_OPTION) {
            return;
        }

        String client = txtClient.getText();
        String product = txtProduct.getText();
        String quantity = txtQuantity.getText();
        String price = txtPrice.getText();
        String address = txtAddress.getText();

        btnAdd.setEnabled(false);
        AsyncExecutor.supplyAsync(
                () -> {
                    try {
                        orderService.register(client, product, quantity, price, address);
                        return true;
                    } catch (SQLException ex) {
                        throw new RuntimeException("No se pudo guardar el pedido en SQLite: " + ex.getMessage(), ex);
                    }
                },
                success -> {
                    btnAdd.setEnabled(true);
                    refreshData();
                },
                error -> {
                    btnAdd.setEnabled(true);
                    String msg = error.getCause() != null ? error.getCause().getMessage() : error.getMessage();
                    JOptionPane.showMessageDialog(this, msg, "Revisa los datos", JOptionPane.WARNING_MESSAGE);
                }
        );
    }
}
