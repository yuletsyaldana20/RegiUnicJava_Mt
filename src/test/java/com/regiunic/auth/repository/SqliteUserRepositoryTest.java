package com.regiunic.auth.repository;

import com.regiunic.auth.model.User;
import com.regiunic.core.config.DatabaseManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Pruebas de Repositorio SQLite de Usuarios (SqliteUserRepository)")
class SqliteUserRepositoryTest {

    @TempDir
    Path tempDir;

    private SqliteUserRepository repository;

    @BeforeEach
    void setUp() throws SQLException {
        Path tempDb = tempDir.resolve("users_repo_test.db");
        DatabaseManager.configureCustomUrl("jdbc:sqlite:" + tempDb.toAbsolutePath().toString().replace('\\', '/'));
        DatabaseManager.initialize();

        repository = new SqliteUserRepository();
    }

    @AfterEach
    void tearDown() {
        DatabaseManager.resetToDefault();
    }

    @Test
    @DisplayName("Debe guardar un usuario y recuperarlo por nombre o por ID")
    void testSaveAndFindUser() throws SQLException {
        User user = new User("admin_test", "salt123", "hash456");
        repository.save(user);

        // Búsqueda por username exacto
        Optional<User> foundByUsername = repository.findByUsername("admin_test");
        assertThat(foundByUsername).isPresent();
        assertThat(foundByUsername.get().username()).isEqualTo("admin_test");
        assertThat(foundByUsername.get().salt()).isEqualTo("salt123");
        assertThat(foundByUsername.get().passwordHash()).isEqualTo("hash456");

        // Búsqueda por ID (delegado de interfaz)
        Optional<User> foundById = repository.findById("admin_test");
        assertThat(foundById).isEqualTo(foundByUsername);
    }

    @Test
    @DisplayName("La búsqueda debe ser insensible a mayúsculas y minúsculas")
    void testFindByUsernameCaseInsensitive() throws SQLException {
        User user = new User("MariaLopez", "saltM", "hashM");
        repository.save(user);

        Optional<User> found = repository.findByUsername("marialopez");
        assertThat(found).isPresent();
        assertThat(found.get().username()).isEqualTo("MariaLopez");
    }

    @Test
    @DisplayName("Debe rechazar el registro de un usuario duplicado")
    void testSaveDuplicateUserThrowsException() throws SQLException {
        User user1 = new User("usuario_unico", "salt1", "hash1");
        repository.save(user1);

        User user2 = new User("usuario_unico", "salt2", "hash2");
        assertThatThrownBy(() -> repository.save(user2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ya está registrado");
    }

    @Test
    @DisplayName("Debe retornar Optional.empty() si el usuario no existe")
    void testFindNonExistentUser() throws SQLException {
        Optional<User> result = repository.findByUsername("fantasma");
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Debe retornar Optional.empty() para búsquedas nulas o vacías")
    void testFindBlankUser() throws SQLException {
        assertThat(repository.findByUsername(null)).isEmpty();
        assertThat(repository.findByUsername("   ")).isEmpty();
        assertThat(repository.findById(null)).isEmpty();
    }
}
