package com.regiunic.orders.ui;

import com.regiunic.core.concurrent.AsyncExecutor;
import com.regiunic.core.ui.AppTheme;
import com.regiunic.core.ui.ViewNavigator;
import com.regiunic.core.ui.components.AnimatedButton;
import com.regiunic.orders.model.Order;
import com.regiunic.orders.service.OrderService;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.List;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Pantalla de reportes con filtros por período y opciones de exportación.
 * <p>Este componente forma parte de la arquitectura modular de RegiUnic.
 */
public class ReportsView extends JPanel {
    private final ViewNavigator navigator;
    private final OrderService service;
    private final JLabel lblVentas = metric("Ventas registradas");
    private final JLabel lblCompletadas = metric("Ventas completadas");
    private final JLabel lblTotal = metric("Total vendido");
    private final JLabel lblPendientes = metric("Pedidos pendientes");
    private final JTextField desde = new JTextField(10);
    private final JTextField hasta = new JTextField(10);

/** Crea e inicializa este componente. */
    public ReportsView(ViewNavigator navigator, OrderService service) {
        this.navigator = navigator;
        this.service = service;
        setLayout(new BorderLayout(10,10));
        setBackground(AppTheme.WINDOW_BACKGROUND);
        initUI();
    }

    private JLabel metric(String text) {
        JLabel label = new JLabel("<html><center>" + text + "<br><b>0</b></center></html>", SwingConstants.CENTER);
        label.setFont(AppTheme.SECTION);
        label.setForeground(AppTheme.NAVY);
        label.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220,225,230)),
                BorderFactory.createEmptyBorder(25,20,25,20)));
        label.setOpaque(true);
        label.setBackground(Color.WHITE);
        return label;
    }

    private void initUI() {
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(AppTheme.PANEL_BACKGROUND);
        top.setBorder(BorderFactory.createEmptyBorder(10,15,10,15));
        AnimatedButton back = AppTheme.primaryButton("← Menú");
        back.addActionListener(e -> navigator.showMainMenu(navigator.getCurrentUser()));
        JLabel title = new JLabel("Reportes y Exportación", SwingConstants.CENTER);
        title.setFont(AppTheme.SECTION);
        title.setForeground(AppTheme.NAVY);
        top.add(back, BorderLayout.WEST);
        top.add(title, BorderLayout.CENTER);
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(top, BorderLayout.NORTH);

        JPanel filters = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
        filters.setOpaque(false);
        desde.setToolTipText("Formato AAAA-MM-DD"); hasta.setToolTipText("Formato AAAA-MM-DD");
        filters.add(new JLabel("Desde (AAAA-MM-DD):")); filters.add(desde);
        filters.add(new JLabel("Hasta (AAAA-MM-DD):")); filters.add(hasta);
        AnimatedButton apply = AppTheme.secondaryButton("Aplicar fechas");
        apply.addActionListener(e -> loadReport()); filters.add(apply);
        header.add(filters, BorderLayout.SOUTH);
        add(header, BorderLayout.NORTH);

        JPanel cards = new JPanel(new GridLayout(2,2,20,20));
        cards.setOpaque(false);
        cards.setBorder(BorderFactory.createEmptyBorder(35,45,25,45));
        cards.add(lblVentas); cards.add(lblCompletadas); cards.add(lblTotal); cards.add(lblPendientes);
        add(cards, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER,15,12));
        bottom.setBackground(AppTheme.WINDOW_BACKGROUND);
        AnimatedButton refresh = AppTheme.secondaryButton("Actualizar reporte");
        AnimatedButton export = AppTheme.primaryButton("Exportar CSV");
        refresh.addActionListener(e -> loadReport());
        export.addActionListener(e -> exportCsv());
        bottom.add(refresh); bottom.add(export);
        add(bottom, BorderLayout.SOUTH);
    }

/** Consulta y devuelve los datos correspondientes. */
    public void loadReport() {
        AsyncExecutor.supplyAsync(
                () -> {
                    try { return service.listOrders(); }
                    catch (SQLException e) { throw new RuntimeException(e); }
                },
                this::showReport,
                error -> JOptionPane.showMessageDialog(this, "No se pudo generar el reporte: " + error.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE));
    }

    private List<Order> currentOrders = List.of();

    private void showReport(List<Order> orders) {
        try {
            LocalDate start = desde.getText().isBlank() ? null : LocalDate.parse(desde.getText().trim());
            LocalDate end = hasta.getText().isBlank() ? null : LocalDate.parse(hasta.getText().trim());
            if (start != null && end != null && start.isAfter(end)) throw new IllegalArgumentException("La fecha inicial no puede ser posterior a la fecha final.");
            currentOrders = orders.stream().filter(o -> o.createdAt() != null)
                    .filter(o -> start == null || !o.createdAt().toLocalDate().isBefore(start))
                    .filter(o -> end == null || !o.createdAt().toLocalDate().isAfter(end)).toList();
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Usa el formato de fecha AAAA-MM-DD, por ejemplo 2026-11-24.", "Fecha inválida", JOptionPane.WARNING_MESSAGE);
            return;
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Rango inválido", JOptionPane.WARNING_MESSAGE);
            return;
        }
        orders = currentOrders;
        long completed = orders.stream().filter(o -> "Completado".equalsIgnoreCase(o.status())).count();
        long pending = orders.stream().filter(o -> "Pendiente".equalsIgnoreCase(o.status())).count();
        double total = orders.stream().mapToDouble(Order::total).sum();
        lblVentas.setText("<html><center>Ventas registradas<br><b>" + orders.size() + "</b></center></html>");
        lblCompletadas.setText("<html><center>Ventas completadas<br><b>" + completed + "</b></center></html>");
        lblTotal.setText("<html><center>Total vendido<br><b>$ " + String.format("%,.2f", total) + "</b></center></html>");
        lblPendientes.setText("<html><center>Pedidos pendientes<br><b>" + pending + "</b></center></html>");
    }

    private void exportCsv() {
        if (currentOrders.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay datos para exportar.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("reporte_regiunic.csv"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        try (PrintWriter out = new PrintWriter(new FileWriter(chooser.getSelectedFile()))) {
            out.println("Cliente,Producto,Cantidad,Precio,Total,Direccion,Estado,Fecha");
            for (Order o : currentOrders) {
                out.printf("\"%s\",\"%s\",%d,%.2f,%.2f,\"%s\",\"%s\",\"%s\"%n",
                        o.client(), o.product(), o.quantity(), o.unitPrice(), o.total(),
                        o.address(), o.status(), o.createdAt());
            }
            JOptionPane.showMessageDialog(this, "Reporte exportado correctamente.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "No se pudo exportar el archivo: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
