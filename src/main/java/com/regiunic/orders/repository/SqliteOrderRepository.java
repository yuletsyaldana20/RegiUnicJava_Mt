package com.regiunic.orders.repository;

import com.regiunic.core.config.DatabaseManager;
import com.regiunic.orders.model.Order;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementación de persistencia para pedidos basada en SQLite con operaciones CRUD completas.
 */
public class SqliteOrderRepository implements OrderRepository {

    @Override
    public List<Order> findAll() throws SQLException {
        List<Order> list = new ArrayList<>();
        String sql = """
            SELECT id, client, product, quantity, unit_price, address, status, created_at
            FROM orders
            ORDER BY created_at DESC
        """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                LocalDateTime date;
                try {
                    date = LocalDateTime.parse(rs.getString("created_at"));
                } catch (Exception e) {
                    date = LocalDateTime.now();
                }

                list.add(new Order(
                        rs.getString("id"),
                        rs.getString("client"),
                        rs.getString("product"),
                        rs.getInt("quantity"),
                        rs.getDouble("unit_price"),
                        rs.getString("address"),
                        rs.getString("status"),
                        date
                ));
            }
        }
        return list;
    }

    @Override
    public Optional<Order> findById(String id) throws SQLException {
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }

        String sql = """
            SELECT id, client, product, quantity, unit_price, address, status, created_at
            FROM orders
            WHERE id = ?
        """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    LocalDateTime date;
                    try {
                        date = LocalDateTime.parse(rs.getString("created_at"));
                    } catch (Exception e) {
                        date = LocalDateTime.now();
                    }

                    return Optional.of(new Order(
                            rs.getString("id"),
                            rs.getString("client"),
                            rs.getString("product"),
                            rs.getInt("quantity"),
                            rs.getDouble("unit_price"),
                            rs.getString("address"),
                            rs.getString("status"),
                            date
                    ));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public void save(Order order) throws SQLException {
        if (order == null) {
            throw new IllegalArgumentException("La orden a guardar no puede ser nula.");
        }

        String sql = """
            INSERT INTO orders (id, client, product, quantity, unit_price, address, status, created_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, order.id());
            stmt.setString(2, order.client());
            stmt.setString(3, order.product());
            stmt.setInt(4, order.quantity());
            stmt.setDouble(5, order.unitPrice());
            stmt.setString(6, order.address());
            stmt.setString(7, order.status());
            stmt.setString(8, order.createdAt().toString());

            stmt.executeUpdate();
        }
    }

    @Override
    public void update(Order order) throws SQLException {
        if (order == null || order.id() == null || order.id().isBlank()) {
            throw new IllegalArgumentException("La orden a actualizar no puede ser nula ni carecer de ID.");
        }

        String sql = """
            UPDATE orders
            SET client = ?, product = ?, quantity = ?, unit_price = ?, address = ?, status = ?
            WHERE id = ?
        """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, order.client());
            stmt.setString(2, order.product());
            stmt.setInt(3, order.quantity());
            stmt.setDouble(4, order.unitPrice());
            stmt.setString(5, order.address());
            stmt.setString(6, order.status());
            stmt.setString(7, order.id().trim());

            int updated = stmt.executeUpdate();
            if (updated == 0) {
                throw new IllegalArgumentException("No se encontró ninguna orden con ID: " + order.id());
            }
        }
    }

    @Override
    public boolean delete(String id) throws SQLException {
        if (id == null || id.isBlank()) {
            return false;
        }

        String sql = "UPDATE orders SET active = 0 WHERE id = ? AND active = 1";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id.trim());
            int affected = stmt.executeUpdate();
            return affected > 0;
        }
    }


    @Override
    public boolean deleteHistory(String id) throws SQLException {
        if (id == null || id.isBlank()) return false;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM orders WHERE id = ?")) {
            stmt.setString(1, id.trim());
            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public List<Order> findActive() throws SQLException {
        List<Order> all = findAll();
        List<Order> active = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT id FROM orders WHERE active = 1");
             ResultSet rs = stmt.executeQuery()) {
            java.util.Set<String> ids = new java.util.HashSet<>();
            while (rs.next()) ids.add(rs.getString(1));
            for (Order order : all) if (ids.contains(order.id())) active.add(order);
        }
        return active;
    }
}
