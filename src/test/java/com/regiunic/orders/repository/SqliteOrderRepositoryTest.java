package com.regiunic.orders.repository;

import com.regiunic.core.config.DatabaseManager;
import com.regiunic.orders.model.Order;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Pruebas de Repositorio SQLite de Órdenes - Operaciones CRUD (SqliteOrderRepository)")
class SqliteOrderRepositoryTest {

    @TempDir
    Path tempDir;

    private SqliteOrderRepository repository;

    @BeforeEach
    void setUp() throws SQLException {
        Path tempDb = tempDir.resolve("orders_repo_test.db");
        DatabaseManager.configureCustomUrl("jdbc:sqlite:" + tempDb.toAbsolutePath().toString().replace('\\', '/'));
        DatabaseManager.initialize();

        repository = new SqliteOrderRepository();
    }

    @AfterEach
    void tearDown() {
        DatabaseManager.resetToDefault();
    }

    @Test
    @DisplayName("Debe retornar lista vacía y Optional.empty() cuando la base de datos está vacía")
    void testEmptyDatabaseQueries() throws SQLException {
        List<Order> orders = repository.findAll();
        assertThat(orders).isNotNull().isEmpty();

        Optional<Order> nonExistent = repository.findById("id-inexistente");
        assertThat(nonExistent).isEmpty();
    }

    @Test
    @DisplayName("Flujo CRUD Completo: Crear, Leer, Actualizar y Eliminar una orden")
    void testCrudOperations() throws SQLException {
        // 1. CREATE (save)
        Order order = new Order("ord-001", "Cliente ABC", "Portatil Dell", 2, 3500000.0, "Calle 123", "Pendiente", LocalDateTime.now());
        repository.save(order);

        // 2. READ (findById y findAll)
        Optional<Order> foundOpt = repository.findById("ord-001");
        assertThat(foundOpt).isPresent();
        Order found = foundOpt.get();
        assertThat(found.client()).isEqualTo("Cliente ABC");
        assertThat(found.product()).isEqualTo("Portatil Dell");
        assertThat(found.quantity()).isEqualTo(2);
        assertThat(found.unitPrice()).isEqualTo(3500000.0);
        assertThat(found.total()).isEqualTo(7000000.0);
        assertThat(found.status()).isEqualTo("Pendiente");

        List<Order> list = repository.findAll();
        assertThat(list).hasSize(1);
        assertThat(list.get(0).id()).isEqualTo("ord-001");

        // 3. UPDATE
        Order updated = new Order("ord-001", "Cliente ABC Modificado", "Portatil Dell Gaming", 3, 3800000.0, "Calle 123 Oficina 402", "Completado", found.createdAt());
        repository.update(updated);

        Optional<Order> reFetchedOpt = repository.findById("ord-001");
        assertThat(reFetchedOpt).isPresent();
        Order reFetched = reFetchedOpt.get();
        assertThat(reFetched.client()).isEqualTo("Cliente ABC Modificado");
        assertThat(reFetched.product()).isEqualTo("Portatil Dell Gaming");
        assertThat(reFetched.quantity()).isEqualTo(3);
        assertThat(reFetched.unitPrice()).isEqualTo(3800000.0);
        assertThat(reFetched.total()).isEqualTo(11400000.0);
        assertThat(reFetched.status()).isEqualTo("Completado");

        // 4. DELETE
        boolean deleted = repository.delete("ord-001");
        assertThat(deleted).isTrue();

        // Verificar que ya no existe
        assertThat(repository.findById("ord-001")).isEmpty();
        assertThat(repository.findAll()).isEmpty();

        // Eliminar nuevamente debe retornar false
        assertThat(repository.delete("ord-001")).isFalse();
    }

    @Test
    @DisplayName("Intentar actualizar una orden que no existe debe lanzar IllegalArgumentException")
    void testUpdateNonExistentOrderThrowsException() {
        Order nonExistent = new Order("ord-fantasma", "Cliente", "Prod", 1, 100.0, "Dir", "Pendiente", LocalDateTime.now());
        assertThatThrownBy(() -> repository.update(nonExistent))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No se encontró ninguna orden");
    }

    @Test
    @DisplayName("save debe rechazar orden nula")
    void testSaveNullOrder() {
        assertThatThrownBy(() -> repository.save(null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
