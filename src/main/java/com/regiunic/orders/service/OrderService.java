package com.regiunic.orders.service;

import com.regiunic.orders.model.Order;
import com.regiunic.orders.repository.OrderRepository;
import com.regiunic.inventory.repository.ProductRepository;
import com.regiunic.inventory.model.Product;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Servicio de negocio para validación, listado, registro, actualización y eliminación de pedidos.
 */
public class OrderService {
    public static final Set<String> VALID_STATUSES = Set.of(
            "Pendiente", "En Proceso", "Completado", "Cancelado"
    );

    private final OrderRepository repository;
    private final ProductRepository products;

/** Crea e inicializa este componente. */
    public OrderService(OrderRepository repository) { this(repository, null); }

/** Crea e inicializa este componente. */
    public OrderService(OrderRepository repository, ProductRepository products) {
        this.repository = repository;
        this.products = products;
    }

/** Consulta y devuelve los datos correspondientes. */
    public List<Order> listOrders() throws SQLException {
        return repository.findAll();
    }

/** Consulta y devuelve los datos correspondientes. */
    public Optional<Order> getOrder(String id) throws SQLException {
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }
        return repository.findById(id.trim());
    }

/** Valida y guarda la información recibida. */
    public Order register(String client, String product, String quantityText, String priceText, String address) throws SQLException {
        if (client == null || client.isBlank() || product == null || product.isBlank() || address == null || address.isBlank()) {
            throw new IllegalArgumentException("Completa cliente, producto y dirección.");
        }

        if (products != null) {
            boolean registered = products.findAll().stream().anyMatch(p -> p.name().equalsIgnoreCase(product.trim()));
            if (!registered) throw new IllegalArgumentException("El producto no está registrado en Gestión de inventario. Regístralo antes de crear el pedido.");
        }
        int quantity = parseQuantity(quantityText);
        double price = parsePrice(priceText);

        Order order = new Order(client.trim(), product.trim(), quantity, price, address.trim());
        repository.save(order);
        return order;
    }

/** Actualiza los datos indicados después de validar la información necesaria. */
    public void updateOrder(String id, String client, String product, String quantityText, String priceText, String address, String status) throws SQLException {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("El ID de la orden es obligatorio.");
        }
        if (client == null || client.isBlank() || product == null || product.isBlank() || address == null || address.isBlank()) {
            throw new IllegalArgumentException("Completa cliente, producto y dirección.");
        }
        validateStatus(status);

        int quantity = parseQuantity(quantityText);
        double price = parsePrice(priceText);

        Optional<Order> existingOpt = repository.findById(id.trim());
        LocalDateTime createdAt = existingOpt.map(Order::createdAt).orElseGet(LocalDateTime::now);

        Order updatedOrder = new Order(id.trim(), client.trim(), product.trim(), quantity, price, address.trim(), status.trim(), createdAt);
        repository.update(updatedOrder);
    }

/** Actualiza los datos indicados después de validar la información necesaria. */
    public void updateOrder(Order order) throws SQLException {
        if (order == null || order.id() == null || order.id().isBlank()) {
            throw new IllegalArgumentException("La orden o su ID no pueden ser nulos.");
        }
        if (order.client() == null || order.client().isBlank() || order.product() == null || order.product().isBlank() || order.address() == null || order.address().isBlank()) {
            throw new IllegalArgumentException("Completa cliente, producto y dirección.");
        }
        if (order.quantity() < 1 || order.unitPrice() <= 0) {
            throw new IllegalArgumentException("La cantidad y el precio deben ser mayores que cero.");
        }
        validateStatus(order.status());

        repository.update(order);
    }

/** Consulta y devuelve los datos correspondientes. */
    public List<Order> listActiveOrders() throws SQLException { return repository.findActive(); }

/** Elimina o desactiva el registro indicado según las reglas del módulo. */
    public boolean deleteHistory(String id) throws SQLException { return repository.deleteHistory(id); }

/** Elimina o desactiva el registro indicado según las reglas del módulo. */
    public boolean deleteOrder(String id) throws SQLException {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("El ID de la orden es obligatorio.");
        }
        return repository.delete(id.trim());
    }

    private int parseQuantity(String quantityText) {
        if (quantityText == null || quantityText.isBlank()) {
            throw new IllegalArgumentException("La cantidad no puede estar vacía.");
        }
        try {
            int quantity = Integer.parseInt(quantityText.trim());
            if (quantity < 1) {
                throw new IllegalArgumentException("La cantidad debe ser mayor que cero.");
            }
            return quantity;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Cantidad y precio deben ser números válidos.");
        }
    }

    private double parsePrice(String priceText) {
        if (priceText == null || priceText.isBlank()) {
            throw new IllegalArgumentException("El precio no puede estar vacío.");
        }
        try {
            double price = Double.parseDouble(priceText.trim().replace(',', '.'));
            if (price <= 0) {
                throw new IllegalArgumentException("El precio debe ser mayor que cero.");
            }
            return price;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Cantidad y precio deben ser números válidos.");
        }
    }

    private void validateStatus(String status) {
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("El estado no puede estar vacío.");
        }
        boolean isValid = VALID_STATUSES.stream()
                .anyMatch(s -> s.equalsIgnoreCase(status.trim()));
        if (!isValid) {
            throw new IllegalArgumentException("Estado inválido: '" + status + "'. Estados permitidos: " + VALID_STATUSES);
        }
    }
}
