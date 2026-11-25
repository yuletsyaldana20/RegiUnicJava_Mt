package com.regiunic.inventory.model;

/**
 * Producto sencillo para el inventario de RegiUnic.
 */
public record Product(int id, String name, int stock, double price, String category) {
}
