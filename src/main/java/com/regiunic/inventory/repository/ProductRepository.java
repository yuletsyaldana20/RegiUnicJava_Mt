package com.regiunic.inventory.repository;

import com.regiunic.inventory.model.Product;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Contrato de acceso a datos para productos.
 * <p>Este componente forma parte de la arquitectura modular de RegiUnic.
 */
public interface ProductRepository {
    List<Product> findAll() throws SQLException;
    Optional<Product> findById(int id) throws SQLException;
    void save(Product product) throws SQLException;
    void update(Product product) throws SQLException;
    boolean delete(int id) throws SQLException;
}
