package com.regiunic.core.config;

import com.regiunic.auth.model.User;
import com.regiunic.auth.repository.SqliteUserRepository;
import com.regiunic.orders.model.Order;
import com.regiunic.orders.repository.SqliteOrderRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@DisplayName("Pruebas de Migración de Datos e Idempotencia (MigrationService)")
class MigrationServiceTest {

    @TempDir
    Path tempDir;

    private SqliteUserRepository userRepository;
    private SqliteOrderRepository orderRepository;
    private Path usersCsv;
    private Path ordersCsv;

    @BeforeEach
    void setUp() throws SQLException {
        Path tempDb = tempDir.resolve("migration_test.db");
        DatabaseManager.configureCustomUrl("jdbc:sqlite:" + tempDb.toAbsolutePath().toString().replace('\\', '/'));
        DatabaseManager.initialize();

        userRepository = new SqliteUserRepository();
        orderRepository = new SqliteOrderRepository();

        usersCsv = tempDir.resolve("usuarios.csv");
        ordersCsv = tempDir.resolve("pedidos.csv");
    }

    @AfterEach
    void tearDown() {
        DatabaseManager.resetToDefault();
    }

    @Test
    @DisplayName("Debe migrar usuarios y pedidos a SQLite y eliminar los archivos CSV originales")
    void testSuccessfulMigrationDeletesCsvFiles() throws IOException, SQLException {
        // Preparar usuario en CSV: Base64("juan_perez"), salt, password_hash
        String encodedUser = Base64.getEncoder().encodeToString("juan_perez".getBytes(StandardCharsets.UTF_8));
        String userCsvContent = encodedUser + ";salt1234;hash5678\n";
        Files.writeString(usersCsv, userCsvContent, StandardCharsets.UTF_8);

        // Preparar pedido en CSV
        String orderCsvContent = """
            id,cliente,producto,cantidad,precio,direccion,estado,fecha,total
            "ord-101","Carlos Gomez","Teclado Mecanico",2,75000.0,"Av Principal #10-20","Pendiente","2026-11-01T10:00:00",150000.0
        """;
        Files.writeString(ordersCsv, orderCsvContent, StandardCharsets.UTF_8);

        // Ejecutar migración con archivos y BD aislados
        MigrationService.initializeAndMigrate(usersCsv, ordersCsv, userRepository, orderRepository);

        // Verificar que los datos fueron insertados en SQLite
        Optional<User> migratedUser = userRepository.findByUsername("juan_perez");
        assertThat(migratedUser).isPresent();
        assertThat(migratedUser.get().username()).isEqualTo("juan_perez");
        assertThat(migratedUser.get().salt()).isEqualTo("salt1234");
        assertThat(migratedUser.get().passwordHash()).isEqualTo("hash5678");

        Optional<Order> migratedOrder = orderRepository.findById("ord-101");
        assertThat(migratedOrder).isPresent();
        assertThat(migratedOrder.get().client()).isEqualTo("Carlos Gomez");
        assertThat(migratedOrder.get().product()).isEqualTo("Teclado Mecanico");
        assertThat(migratedOrder.get().quantity()).isEqualTo(2);
        assertThat(migratedOrder.get().unitPrice()).isEqualTo(75000.0);
        assertThat(migratedOrder.get().total()).isEqualTo(150000.0);

        // Verificar que los archivos CSV fueron eliminados
        assertThat(Files.exists(usersCsv)).isFalse();
        assertThat(Files.exists(ordersCsv)).isFalse();
    }

    @Test
    @DisplayName("La migración debe ser idempotente al ejecutarse una segunda vez sin fallar")
    void testMigrationIsIdempotent() throws IOException, SQLException {
        // 1. Primera ejecución con archivo
        String encodedUser = Base64.getEncoder().encodeToString("admin_test".getBytes(StandardCharsets.UTF_8));
        Files.writeString(usersCsv, encodedUser + ";saltX;hashY\n", StandardCharsets.UTF_8);

        MigrationService.initializeAndMigrate(usersCsv, ordersCsv, userRepository, orderRepository);
        assertThat(userRepository.findByUsername("admin_test")).isPresent();

        // 2. Segunda ejecución cuando los archivos ya no existen
        assertThatCode(() -> MigrationService.initializeAndMigrate(usersCsv, ordersCsv, userRepository, orderRepository))
                .doesNotThrowAnyException();

        // Los datos persisten intactos
        assertThat(userRepository.findByUsername("admin_test")).isPresent();
    }

    @Test
    @DisplayName("Debe ignorar archivos inexistentes o vacíos sin lanzar excepciones")
    void testMigrationWithMissingFiles() {
        Path nonExistentUsers = tempDir.resolve("missing_users.csv");
        Path nonExistentOrders = tempDir.resolve("missing_orders.csv");

        assertThatCode(() -> MigrationService.initializeAndMigrate(nonExistentUsers, nonExistentOrders, userRepository, orderRepository))
                .doesNotThrowAnyException();
    }
}
