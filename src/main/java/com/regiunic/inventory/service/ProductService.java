package com.regiunic.inventory.service;

import com.regiunic.inventory.model.Product;
import com.regiunic.inventory.repository.ProductRepository;

import java.sql.SQLException;
import java.util.List;

/**
 * Servicio de negocio para validar y gestionar productos.
 * <p>Este componente forma parte de la arquitectura modular de RegiUnic.
 */
public class ProductService {
    private final ProductRepository repository;

/** Crea e inicializa este componente. */
    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }

/** Consulta y devuelve los datos correspondientes. */
    public List<Product> listProducts() throws SQLException {
        return repository.findAll();
    }

/** Valida y guarda la información recibida. */
    public void save(String name, String stockText, String priceText, String category) throws SQLException {
        validateText(name, category);
        int stock = parseStock(stockText);
        double price = parsePrice(priceText);
        repository.save(new Product(0, name.trim(), stock, price, category.trim()));
    }

/** Actualiza los datos indicados después de validar la información necesaria. */
    public void update(int id, String name, String stockText, String priceText, String category) throws SQLException {
        validateText(name, category);
        int stock = parseStock(stockText);
        double price = parsePrice(priceText);
        repository.update(new Product(id, name.trim(), stock, price, category.trim()));
    }

/** Elimina o desactiva el registro indicado según las reglas del módulo. */
    public boolean delete(int id) throws SQLException {
        return repository.delete(id);
    }

    private void validateText(String name, String category) {
        if (name == null || name.isBlank() || category == null || category.isBlank()) {
            throw new IllegalArgumentException("Completa el nombre y la categoría.");
        }
    }

    private int parseStock(String text) {
        try {
            int stock = Integer.parseInt(text.trim());
            if (stock < 0) throw new IllegalArgumentException("El stock no puede ser negativo.");
            return stock;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("El stock debe ser un número entero.");
        }
    }

    private double parsePrice(String text) {
        try {
            double price = Double.parseDouble(text.trim().replace(',', '.'));
            if (price <= 0) throw new IllegalArgumentException("El precio debe ser mayor que cero.");
            return price;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("El precio debe ser un número válido.");
        }
    }
}
