package com.regiunic.auth.repository;

import com.regiunic.auth.model.User;

import java.sql.SQLException;
import java.util.Optional;

/**
 * Contrato de persistencia para el acceso y almacenamiento de usuarios.
 */
public interface UserRepository {
    Optional<User> findByUsername(String username) throws SQLException;

    default Optional<User> findById(String id) throws SQLException {
        return findByUsername(id);
    }

    void save(User user) throws SQLException;
}
