# RegiUnic

Aplicación de escritorio Java Swing para gestión de inventario, pedidos/ventas, usuarios e informes.

Antes de comenzar, lee [`GUIA_CODIGO.md`](GUIA_CODIGO.md), donde se explica la arquitectura, la función de cada módulo, las reglas de negocio, las pruebas y cómo compilar el proyecto.

## Requisitos
- JDK 21
- Maven

## Compilar y probar
```bash
mvn clean test
mvn package
```

La base de datos local se encuentra en `data/regiunic.db`. Haz una copia de seguridad antes de modificar datos o migraciones.
