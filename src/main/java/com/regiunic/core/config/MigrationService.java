package com.regiunic.core.config;

import com.regiunic.auth.model.User;
import com.regiunic.auth.repository.SqliteUserRepository;
import com.regiunic.auth.repository.UserRepository;
import com.regiunic.orders.model.Order;
import com.regiunic.orders.repository.OrderRepository;
import com.regiunic.orders.repository.SqliteOrderRepository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * Servicio de inicialización y migración de datos heredados desde archivos CSV hacia SQLite.
 * Utiliza SqliteUserRepository y SqliteOrderRepository para persistir las entidades
 * y elimina los archivos CSV originales una vez completada la migración.
 */
public final class MigrationService {
    public static final Path DEFAULT_USERS_CSV = Path.of("data", "usuarios.csv");
    public static final Path DEFAULT_ORDERS_CSV = Path.of("data", "pedidos.csv");

    private MigrationService() {}

    /**
     * Método de inicialización por defecto que detecta datos legados en CSV de la carpeta data/,
     * los parsea, los inserta en SQLite y posteriormente elimina los archivos CSV.
     */
    public static void initializeAndMigrate() {
        initializeAndMigrate(new SqliteUserRepository(), new SqliteOrderRepository());
    }

    /**
     * Sobrecarga para permitir inyección de dependencias de repositorios con rutas por defecto.
     */
    public static void initializeAndMigrate(UserRepository userRepository, OrderRepository orderRepository) {
        initializeAndMigrate(DEFAULT_USERS_CSV, DEFAULT_ORDERS_CSV, userRepository, orderRepository);
    }

    /**
     * Sobrecarga completa que permite inyectar rutas de archivos CSV para pruebas aisladas y repositorios.
     */
    public static void initializeAndMigrate(Path usersCsv, Path ordersCsv, UserRepository userRepository, OrderRepository orderRepository) {
        try {
            migrateUsers(usersCsv, userRepository);
            migrateOrders(ordersCsv, orderRepository);
        } catch (Exception ex) {
            System.err.println("Advertencia durante la migración de archivos CSV a SQLite: " + ex.getMessage());
        }
    }

    /**
     * Alias compatible con el punto de entrada de la aplicación.
     */
    public static void migrateIfLegacyExists() {
        initializeAndMigrate();
    }

    private static void migrateUsers(Path usersCsv, UserRepository userRepository) throws IOException, SQLException {
        if (usersCsv == null || !Files.exists(usersCsv)) {
            return;
        }

        List<String> lines = Files.readAllLines(usersCsv, StandardCharsets.UTF_8);
        boolean hasRecords = false;

        for (String line : lines) {
            if (line.isBlank()) {
                continue;
            }
            String[] fields = line.split(";", -1);
            if (fields.length != 3) {
                continue;
            }

            try {
                String username = new String(Base64.getDecoder().decode(fields[0]), StandardCharsets.UTF_8).trim();
                String salt = fields[1].trim();
                String passwordHash = fields[2].trim();

                if (!username.isEmpty() && userRepository.findByUsername(username).isEmpty()) {
                    userRepository.save(new User(username, salt, passwordHash));
                }
                hasRecords = true;
            } catch (Exception ex) {
                System.err.println("Error al migrar usuario legacy: " + ex.getMessage());
            }
        }

        // Una vez insertados exitosamente en SQLite, se elimina el archivo CSV
        if (hasRecords || lines.isEmpty() || lines.stream().allMatch(String::isBlank)) {
            Files.deleteIfExists(usersCsv);
            System.out.println("Migración de usuarios completada. Archivo '" + usersCsv + "' eliminado.");
        }
    }

    private static void migrateOrders(Path ordersCsv, OrderRepository orderRepository) throws IOException, SQLException {
        if (ordersCsv == null || !Files.exists(ordersCsv)) {
            return;
        }

        List<String> lines = Files.readAllLines(ordersCsv, StandardCharsets.UTF_8);
        boolean hasRecords = false;

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isBlank() || trimmed.toLowerCase().startsWith("id,") || trimmed.toLowerCase().startsWith("id;")) {
                continue;
            }

            List<String> cols = parseCsvLine(trimmed);
            if (cols.size() < 8) {
                continue;
            }

            try {
                String id = cols.get(0).trim();
                String client = cols.get(1).trim();
                String product = cols.get(2).trim();
                int quantity = Integer.parseInt(cols.get(3).trim());
                double unitPrice = Double.parseDouble(cols.get(4).trim().replace(',', '.'));
                String address = cols.get(5).trim();
                String status = cols.get(6).trim();
                String createdAtRaw = cols.get(7).trim();

                LocalDateTime createdAt;
                try {
                    createdAt = LocalDateTime.parse(createdAtRaw);
                } catch (Exception e) {
                    createdAt = LocalDateTime.now();
                }

                Order order = new Order(id, client, product, quantity, unitPrice, address, status, createdAt);

                if (orderRepository.findById(id).isEmpty()) {
                    orderRepository.save(order);
                }
                hasRecords = true;
            } catch (Exception ex) {
                System.err.println("Error al migrar pedido legacy: " + ex.getMessage());
            }
        }

        // Una vez insertados exitosamente en SQLite, se elimina el archivo CSV
        if (hasRecords || lines.isEmpty() || lines.stream().allMatch(l -> l.isBlank() || l.startsWith("id,") || l.startsWith("id;"))) {
            Files.deleteIfExists(ordersCsv);
            System.out.println("Migración de pedidos completada. Archivo '" + ordersCsv + "' eliminado.");
        }
    }

    private static List<String> parseCsvLine(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder value = new StringBuilder();
        boolean quoted = false;

        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == '"' && quoted && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                value.append('"');
                i++;
            } else if (ch == '"') {
                quoted = !quoted;
            } else if (ch == ',' && !quoted) {
                values.add(value.toString());
                value.setLength(0);
            } else {
                value.append(ch);
            }
        }
        values.add(value.toString());
        return values;
    }
}
