package com.regiunic.core.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@DisplayName("Pruebas de Inicialización y Ciclo de Vida de DatabaseManager")
class DatabaseManagerTest {

    @TempDir
    Path tempDir;

    private String tempJdbcUrl;

    @BeforeEach
    void setUp() {
        Path tempDbPath = tempDir.resolve("test_database.db");
        tempJdbcUrl = "jdbc:sqlite:" + tempDbPath.toAbsolutePath().toString().replace('\\', '/');
        DatabaseManager.configureCustomUrl(tempJdbcUrl);
    }

    @AfterEach
    void tearDown() {
        DatabaseManager.resetToDefault();
    }

    @Test
    @DisplayName("Debe crear las tablas users y orders e índices en una BD limpia")
    void testInitializeCreatesSchema() throws SQLException {
        DatabaseManager.initialize();

        try (Connection conn = DatabaseManager.getConnection()) {
            assertThat(conn).isNotNull();
            assertThat(conn.isClosed()).isFalse();

            // Verificar tablas creadas
            DatabaseMetaData metaData = conn.getMetaData();
            Set<String> tables = new HashSet<>();
            try (ResultSet rs = metaData.getTables(null, null, "%", new String[]{"TABLE"})) {
                while (rs.next()) {
                    tables.add(rs.getString("TABLE_NAME").toLowerCase());
                }
            }
            assertThat(tables).contains("users", "orders");

            // Verificar soporte de llaves foráneas activado
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("PRAGMA foreign_keys;")) {
                if (rs.next()) {
                    assertThat(rs.getInt(1)).isEqualTo(1);
                }
            }
        }
    }

    @Test
    @DisplayName("La inicialización debe ser idempotente y no fallar al ejecutarse repetidas veces")
    void testInitializeIsIdempotent() {
        assertThatCode(() -> {
            DatabaseManager.initialize();
            DatabaseManager.initialize();
            DatabaseManager.initialize();
        }).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("resetToDefault debe restaurar la URL oficial de producción")
    void testResetToDefault() {
        DatabaseManager.resetToDefault();
        assertThat(DatabaseManager.getJdbcUrl()).contains("regiunic.db");
    }
}
