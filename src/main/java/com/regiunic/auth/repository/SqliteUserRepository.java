package com.regiunic.auth.repository;

import com.regiunic.auth.model.User;
import com.regiunic.core.config.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Implementación de persistencia para usuarios utilizando SQLite mediante JDBC.
 */
public class SqliteUserRepository implements UserRepository {

    @Override
    public Optional<User> findByUsername(String username) throws SQLException {
        if (username == null || username.isBlank()) {
            return Optional.empty();
        }

        String sql = "SELECT username, salt, password_hash FROM users WHERE LOWER(username) = LOWER(?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new User(
                            rs.getString("username"),
                            rs.getString("salt"),
                            rs.getString("password_hash")
                    ));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public void save(User user) throws SQLException {
        if (findByUsername(user.username()).isPresent()) {
            throw new IllegalArgumentException("Ese nombre de usuario ya está registrado.");
        }

        String sql = "INSERT INTO users (username, salt, password_hash) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.username().trim());
            stmt.setString(2, user.salt());
            stmt.setString(3, user.passwordHash());
            stmt.executeUpdate();
        }
    }
}
