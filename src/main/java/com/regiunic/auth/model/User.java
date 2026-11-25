package com.regiunic.auth.model;

/**
 * Representa una cuenta de usuario sin conservar la contraseña original en texto claro.
 * Modelo inmutable usando Records de Java.
 */
public record User(String username, String salt, String passwordHash) {
}
