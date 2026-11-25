package com.regiunic.inventory.repository;

import com.regiunic.core.config.DatabaseManager;
import com.regiunic.inventory.model.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementación SQLite del almacenamiento y consulta de productos.
 * <p>Este componente forma parte de la arquitectura modular de RegiUnic.
 */
public class SqliteProductRepository implements ProductRepository {

    @Override
    public List<Product> findAll() throws SQLException {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT id, name, stock, price, category FROM products ORDER BY name";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                products.add(new Product(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("stock"),
                        rs.getDouble("price"),
                        rs.getString("category")
                ));
            }
        }
        return products;
    }

    @Override
    public Optional<Product> findById(int id) throws SQLException {
        String sql = "SELECT id, name, stock, price, category FROM products WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Product(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getInt("stock"),
                            rs.getDouble("price"),
                            rs.getString("category")
                    ));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public void save(Product product) throws SQLException {
        String sql = "INSERT INTO products (name, stock, price, category) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, product.name().trim());
            stmt.setInt(2, product.stock());
            stmt.setDouble(3, product.price());
            stmt.setString(4, product.category().trim());
            stmt.executeUpdate();
        }
    }

    @Override
    public void update(Product product) throws SQLException {
        String sql = "UPDATE products SET name=?, stock=?, price=?, category=? WHERE id=?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, product.name().trim());
            stmt.setInt(2, product.stock());
            stmt.setDouble(3, product.price());
            stmt.setString(4, product.category().trim());
            stmt.setInt(5, product.id());
            if (stmt.executeUpdate() == 0) {
                throw new IllegalArgumentException("No se encontró el producto.");
            }
        }
    }

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM products WHERE id=?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }
}
