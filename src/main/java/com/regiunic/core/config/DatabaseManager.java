package com.regiunic.core.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Administrador central de la conexión y ciclo de vida de la base de datos SQLite.
 * Gestiona la inicialización de esquemas, tablas e índices.
 * Soporta configuración de URL personalizada para pruebas aisladas (in-memory o archivo temporal).
 */
public final class DatabaseManager {
    private static final String DEFAULT_DB_DIR = "data";
    private static final String DEFAULT_DB_NAME = "regiunic.db";
    private static final String DEFAULT_DB_PATH = DEFAULT_DB_DIR + "/" + DEFAULT_DB_NAME;
    private static final String DEFAULT_JDBC_URL = "jdbc:sqlite:" + DEFAULT_DB_PATH;

    private static String customJdbcUrl = null;
    private static boolean initialized = false;

    private DatabaseManager() {}

    /**
     * Configura una URL JDBC personalizada (ej. en memoria 'jdbc:sqlite::memory:' o archivo temporal).
     * Reinicia el estado de inicialización para permitir configurar el nuevo esquema.
     */
    public static synchronized void configureCustomUrl(String jdbcUrl) {
        customJdbcUrl = jdbcUrl;
        initialized = false;
    }

    /**
     * Restaura la configuración a la base de datos de producción predeterminada.
     */
    public static synchronized void resetToDefault() {
        customJdbcUrl = null;
        initialized = false;
    }

    /**
     * Retorna la URL JDBC actualmente en uso.
     */
    public static String getJdbcUrl() {
        return customJdbcUrl != null ? customJdbcUrl : DEFAULT_JDBC_URL;
    }

    /**
     * Obtiene una nueva conexión a SQLite configurando soporte WAL y claves foráneas.
     */
    public static Connection getConnection() throws SQLException {
        ensureInitialized();
        Connection connection = DriverManager.getConnection(getJdbcUrl());
        try (Statement st = connection.createStatement()) {
            st.execute("PRAGMA foreign_keys = ON;");
            // No activar WAL en bases de datos puramente en memoria
            if (!getJdbcUrl().contains(":memory:")) {
                st.execute("PRAGMA journal_mode = WAL;");
            }
        }
        return connection;
    }

    /**
     * Inicializa las tablas e índices si no existen.
     */
    public static synchronized void initialize() throws SQLException {
        String activeUrl = getJdbcUrl();

        // Si es una ruta de archivo y no memoria pura, asegurar directorio padre
        if (!activeUrl.contains(":memory:")) {
            String pathStr = activeUrl.startsWith("jdbc:sqlite:")
                    ? activeUrl.substring("jdbc:sqlite:".length())
                    : activeUrl;
            Path dbPath = Path.of(pathStr);
            Path parentDir = dbPath.getParent();
            if (parentDir != null) {
                try {
                    Files.createDirectories(parentDir);
                } catch (IOException e) {
                    throw new SQLException("No se pudo crear el directorio para la base de datos: " + parentDir, e);
                }
            }
        }

        try (Connection conn = DriverManager.getConnection(activeUrl);
             Statement stmt = conn.createStatement()) {

            stmt.execute("PRAGMA foreign_keys = ON;");
            if (!activeUrl.contains(":memory:")) {
                stmt.execute("PRAGMA journal_mode = WAL;");
            }

            // Tabla de Usuarios
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    username TEXT PRIMARY KEY,
                    salt TEXT NOT NULL,
                    password_hash TEXT NOT NULL,
                    created_at TEXT DEFAULT (datetime('now', 'localtime'))
                );
            """);

            // Tabla de Pedidos / Ventas
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS orders (
                    id TEXT PRIMARY KEY,
                    client TEXT NOT NULL,
                    product TEXT NOT NULL,
                    quantity INTEGER NOT NULL CHECK(quantity > 0),
                    unit_price REAL NOT NULL CHECK(unit_price > 0),
                    address TEXT NOT NULL,
                    status TEXT NOT NULL DEFAULT 'Pendiente',
                    created_at TEXT NOT NULL
                );
            """);

            // Soft delete: los pedidos eliminados de la lista siguen conservados en historial.
            try { stmt.execute("ALTER TABLE orders ADD COLUMN active INTEGER NOT NULL DEFAULT 1"); }
            catch (SQLException ignored) { /* columna ya existente */ }

            // Tabla de inventario
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS products (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    stock INTEGER NOT NULL CHECK(stock >= 0),
                    price REAL NOT NULL CHECK(price > 0),
                    category TEXT NOT NULL
                );
            """);

            stmt.execute("CREATE INDEX IF NOT EXISTS idx_products_name ON products(name);");

            // Índices para optimizar búsquedas frecuentes
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_orders_client ON orders(client);");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_orders_created_at ON orders(created_at);");
        }
        initialized = true;
    }

    private static synchronized void ensureInitialized() throws SQLException {
        if (!initialized) {
            initialize();
        }
    }

/** Consulta y devuelve los datos correspondientes. */
    public static String getDatabasePath() {
        return customJdbcUrl != null ? customJdbcUrl : DEFAULT_DB_PATH;
    }
}
