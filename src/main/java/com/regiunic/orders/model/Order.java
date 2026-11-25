package com.regiunic.orders.model;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Modelo inmutable que representa un pedido o venta en el sistema.
 * Implementado como Record de Java.
 */
public record Order(
        String id,
        String client,
        String product,
        int quantity,
        double unitPrice,
        String address,
        String status,
        LocalDateTime createdAt
) {
/** Crea e inicializa este componente. */
    public Order(String client, String product, int quantity, double unitPrice, String address) {
        this(UUID.randomUUID().toString(), client, product, quantity, unitPrice, address,
                "Pendiente", LocalDateTime.now());
    }

/** Ejecuta la operación indicada por este método. */
    public double total() {
        return quantity * unitPrice;
    }
}
