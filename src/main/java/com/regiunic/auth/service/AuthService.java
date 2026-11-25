package com.regiunic.auth.service;

import com.regiunic.auth.model.User;
import com.regiunic.auth.repository.UserRepository;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;

/**
 * Servicio de negocio para el registro, validación y autenticación de usuarios.
 */
public class AuthService {
    private static final int ITERATIONS = 210_000;
    private static final int SALT_LENGTH = 16;
    private static final int KEY_LENGTH = 256;

    private final UserRepository repository;
    private final SecureRandom secureRandom = new SecureRandom();

/** Crea e inicializa este componente. */
    public AuthService(UserRepository repository) {
        this.repository = repository;
    }

/** Valida y guarda la información recibida. */
    public void register(String username, char[] password, char[] confirmation) throws SQLException {
        String cleanUsername = username == null ? "" : username.trim();
        if (cleanUsername.isEmpty()) {
            throw new IllegalArgumentException("Escribe un nombre de usuario.");
        }
        if (password == null || password.length < 8) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres.");
        }
        if (confirmation == null || !Arrays.equals(password, confirmation)) {
            throw new IllegalArgumentException("Las contraseñas no coinciden.");
        }
        if (repository.findByUsername(cleanUsername).isPresent()) {
            throw new IllegalArgumentException("Ese nombre de usuario ya está registrado.");
        }

        byte[] salt = new byte[SALT_LENGTH];
        secureRandom.nextBytes(salt);
        byte[] hash = hashPassword(password, salt);
        try {
            User user = new User(
                    cleanUsername,
                    Base64.getEncoder().encodeToString(salt),
                    Base64.getEncoder().encodeToString(hash)
            );
            repository.save(user);
        } finally {
            Arrays.fill(hash, (byte) 0);
        }
    }

/** Ejecuta la operación indicada por este método. */
    public boolean authenticate(String username, char[] password) throws SQLException {
        if (username == null || username.isBlank() || password == null || password.length == 0) {
            return false;
        }

        Optional<User> userResult = repository.findByUsername(username.trim());
        if (userResult.isEmpty()) {
            return false;
        }

        User user = userResult.get();
        byte[] salt = Base64.getDecoder().decode(user.salt());
        byte[] expectedHash = Base64.getDecoder().decode(user.passwordHash());
        byte[] actualHash = hashPassword(password, salt);
        try {
            return MessageDigest.isEqual(expectedHash, actualHash);
        } finally {
            Arrays.fill(actualHash, (byte) 0);
        }
    }

    private byte[] hashPassword(char[] password, byte[] salt) {
        PBEKeySpec keySpec = new PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(keySpec)
                    .getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
            throw new IllegalStateException("No se pudo proteger la contraseña con el algoritmo configurado.", ex);
        } finally {
            keySpec.clearPassword();
        }
    }
}
