package com.regiunic.orders.repository;

import com.regiunic.orders.model.Order;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Contrato de persistencia para la gestión integral de pedidos y ventas (CRUD).
 */
public interface OrderRepository {
    List<Order> findAll() throws SQLException;
    Optional<Order> findById(String id) throws SQLException;
    void save(Order order) throws SQLException;
    void update(Order order) throws SQLException;
    boolean delete(String id) throws SQLException;
    boolean deleteHistory(String id) throws SQLException;
    List<Order> findActive() throws SQLException;
}
