package com.regiunic.auth.service;

import com.regiunic.auth.model.User;
import com.regiunic.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas Unitarias de Lógica de Autenticación (AuthService)")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository);
    }

    @Test
    @DisplayName("Debe registrar un usuario exitosamente asegurando que la contraseña NUNCA se guarde en texto plano")
    void testRegisterHashesPasswordSecurely() throws SQLException {
        when(userRepository.findByUsername("nuevo_usuario")).thenReturn(Optional.empty());

        char[] password = "PasswordSeguro2026!".toCharArray();
        char[] confirmation = "PasswordSeguro2026!".toCharArray();

        authService.register("nuevo_usuario", password, confirmation);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());

        User savedUser = captor.getValue();
        assertThat(savedUser.username()).isEqualTo("nuevo_usuario");
        // Verificar que no se guardó el texto plano
        assertThat(savedUser.passwordHash()).isNotEqualTo("PasswordSeguro2026!");
        assertThat(savedUser.salt()).isNotBlank();
        assertThat(savedUser.passwordHash()).isNotBlank();
        // El hash y salt son base64 válidos
        assertThat(savedUser.salt()).matches("^[A-Za-z0-9+/=]+$");
        assertThat(savedUser.passwordHash()).matches("^[A-Za-z0-9+/=]+$");
    }

    @Test
    @DisplayName("Debe rechazar el registro con nombre de usuario vacío o nulo")
    void testRegisterWithInvalidUsername() {
        char[] pass = "12345678".toCharArray();

        assertThatThrownBy(() -> authService.register(null, pass, pass))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Escribe un nombre de usuario");

        assertThatThrownBy(() -> authService.register("   ", pass, pass))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Escribe un nombre de usuario");
    }

    @Test
    @DisplayName("Debe rechazar contraseñas con longitud menor a 8 caracteres")
    void testRegisterWithShortPassword() {
        char[] pass = "corta".toCharArray();

        assertThatThrownBy(() -> authService.register("usuario1", pass, pass))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("al menos 8 caracteres");
    }

    @Test
    @DisplayName("Debe rechazar el registro si las contraseñas no coinciden")
    void testRegisterWithMismatchedPasswords() {
        char[] pass = "Password123".toCharArray();
        char[] confirm = "PasswordDiferente".toCharArray();

        assertThatThrownBy(() -> authService.register("usuario1", pass, confirm))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Las contraseñas no coinciden");
    }

    @Test
    @DisplayName("Debe rechazar el registro si el nombre de usuario ya existe")
    void testRegisterWithExistingUsername() throws SQLException {
        when(userRepository.findByUsername("usuario_existente"))
                .thenReturn(Optional.of(new User("usuario_existente", "salt", "hash")));

        char[] pass = "PasswordValido123".toCharArray();

        assertThatThrownBy(() -> authService.register("usuario_existente", pass, pass))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ya está registrado");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("authenticate debe retornar true cuando las credenciales son correctas")
    void testAuthenticateSuccess() throws SQLException {
        // Primero registramos un usuario en un repositorio simulado para obtener salt y hash válidos
        AuthService serviceWithMock = new AuthService(userRepository);
        when(userRepository.findByUsername("test_user")).thenReturn(Optional.empty());

        char[] password = "MiPasswordSecreto1".toCharArray();
        serviceWithMock.register("test_user", password, password);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User registeredUser = captor.getValue();

        // Ahora simulamos que findByUsername devuelve el usuario registrado
        when(userRepository.findByUsername("test_user")).thenReturn(Optional.of(registeredUser));

        boolean authenticated = serviceWithMock.authenticate("test_user", "MiPasswordSecreto1".toCharArray());
        assertThat(authenticated).isTrue();
    }

    @Test
    @DisplayName("authenticate debe retornar false si la contraseña es incorrecta")
    void testAuthenticateWrongPassword() throws SQLException {
        when(userRepository.findByUsername("test_user")).thenReturn(Optional.empty());
        char[] password = "PasswordOriginal123".toCharArray();
        authService.register("test_user", password, password);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User registeredUser = captor.getValue();

        when(userRepository.findByUsername("test_user")).thenReturn(Optional.of(registeredUser));

        boolean authenticated = authService.authenticate("test_user", "PasswordEquivocado".toCharArray());
        assertThat(authenticated).isFalse();
    }

    @Test
    @DisplayName("authenticate debe retornar false para usuarios inexistentes o credenciales vacías")
    void testAuthenticateEdgeCases() throws SQLException {
        when(userRepository.findByUsername("inexistente")).thenReturn(Optional.empty());

        assertThat(authService.authenticate("inexistente", "cualquierClave".toCharArray())).isFalse();
        assertThat(authService.authenticate(null, "clave".toCharArray())).isFalse();
        assertThat(authService.authenticate("", "clave".toCharArray())).isFalse();
        assertThat(authService.authenticate("user", null)).isFalse();
        assertThat(authService.authenticate("user", new char[0])).isFalse();
    }
}
