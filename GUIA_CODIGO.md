# RegiUnic — Guía de código y mantenimiento

## 1. ¿Qué es RegiUnic?

RegiUnic es una aplicación de escritorio Java Swing para gestionar usuarios, productos de inventario y pedidos/ventas. La información se almacena en SQLite. El proyecto usa Maven para administrar dependencias y ejecutar pruebas.

## 2. Estructura del proyecto

- `src/main/java/com/regiunic/App.java`: punto de entrada; configura la aplicación y conecta los componentes.
- `auth/`: inicio de sesión, registro de usuarios, modelo, servicio y repositorio de usuarios.
- `inventory/`: modelo de producto, reglas de negocio, persistencia y pantalla de inventario.
- `orders/`: modelo de pedido, reglas de negocio, persistencia y pantallas de pedidos, historial, filtros y reportes.
- `core/config/`: inicialización de SQLite y migraciones de datos antiguos.
- `core/ui/`: ventana principal, tema visual, navegación y componentes reutilizables.
- `src/main/resources/icons/`: iconos SVG utilizados por la interfaz.
- `src/test/java/`: pruebas automatizadas.
- `data/regiunic.db`: base de datos local de la aplicación.
- `pom.xml`: configuración Maven, dependencias y opciones de compilación.

## 3. Arquitectura en capas

El código se divide en capas para que cada archivo tenga una responsabilidad clara:

1. **Modelo (`model`)**: representa los datos mediante clases como `Product`, `Order` y `User`.
2. **Repositorio (`repository`)**: lee y escribe datos en SQLite. Las interfaces (`ProductRepository`, `OrderRepository`, `UserRepository`) describen las operaciones disponibles; las clases `Sqlite...Repository` implementan esas operaciones.
3. **Servicio (`service`)**: aplica las reglas de negocio y valida los datos antes de guardarlos.
4. **Interfaz (`ui`)**: muestra formularios, tablas, botones y mensajes al usuario.
5. **Configuración (`core/config`)**: crea la base de datos y prepara las migraciones.
6. **Componentes comunes (`core/ui`)**: reúnen estilos, navegación y utilidades compartidas.

Flujo habitual: **Pantalla → Servicio → Repositorio → SQLite**. Las consultas de datos regresan por la misma ruta hasta la pantalla.

## 4. Archivos principales

### Inicio y configuración
- `App.java`: inicia Swing en el hilo de eventos, aplica el tema, inicializa SQLite, crea repositorios y servicios, ejecuta migraciones y abre `MainFrame`.
- `DatabaseManager.java`: prepara el archivo y el esquema de la base de datos.
- `MigrationService.java`: importa datos de formatos anteriores cuando corresponde.
- `AppTheme.java`: define estilos visuales compartidos.

### Usuarios
- `User.java`: estructura de datos del usuario.
- `UserRepository.java`: operaciones que debe ofrecer un repositorio de usuarios.
- `SqliteUserRepository.java`: consultas SQLite para usuarios.
- `AuthService.java`: reglas de autenticación.
- `LoginView.java` y `RegisterDialog.java`: pantallas de acceso y registro.

### Inventario
- `Product.java`: contiene los datos de un producto.
- `ProductRepository.java`: contrato de persistencia del inventario.
- `SqliteProductRepository.java`: implementación de consultas SQLite.
- `ProductService.java`: validaciones y operaciones de negocio de productos.
- `InventoryView.java`: formulario y tabla para administrar productos.

### Pedidos, historial y reportes
- `Order.java`: contiene la información de un pedido, incluido su estado y fecha.
- `OrderRepository.java`: define las operaciones de persistencia de pedidos.
- `SqliteOrderRepository.java`: ejecuta consultas y actualizaciones de pedidos en SQLite.
- `OrderService.java`: valida cliente, producto, cantidad, precio y estado. También comprueba que el producto exista en el inventario al registrar un pedido cuando se ha proporcionado el repositorio de productos.
- `OrdersView.java`: pantalla de pedidos activos.
- `HistoryView.java`: consulta y administración del historial.
- `ReportsView.java`: reportes y exportación.
- `FilterView.java`: filtros de consulta.
- `MainMenuView.java`: navegación del módulo de pedidos.

### Utilidades visuales
- `MainFrame.java`: ventana contenedora de la aplicación.
- `ViewNavigator.java`: ayuda a cambiar de vista.
- `AnimatedButton.java`: botón reutilizable.
- `ModernScrollBarUI.java`: apariencia de barras de desplazamiento.
- `PlaceholderView.java`: vista temporal.
- `AsyncExecutor.java`: ejecución de tareas en segundo plano para no bloquear la interfaz.

## 5. Reglas importantes del negocio

- **Productos válidos**: un pedido nuevo debe usar un producto que esté registrado en inventario.
- **Validación de cantidad**: la cantidad debe ser un entero mayor que cero.
- **Validación de precio**: el precio debe ser numérico y mayor que cero; se acepta coma decimal y se normaliza a punto.
- **Estado del pedido**: se aceptan los estados definidos en `OrderService.VALID_STATUSES`.
- **Baja lógica**: retirar un pedido de la lista activa debe conservar el registro en la base de datos para el historial.
- **Borrado del historial**: la eliminación definitiva debe reservarse para la acción explícita del usuario desde el historial.
- **Fechas de reportes**: el intervalo debe incluir la fecha inicial y final elegidas; revisar el método de consulta/exportación correspondiente si se cambia el formato.
- **Persistencia**: las operaciones SQL deben mantenerse en los repositorios, no dentro de los componentes visuales.

## 6. Cómo agregar una función nueva

Ejemplo: agregar una validación a los pedidos.

1. Identifica la regla y ubica el método correspondiente en `OrderService`.
2. Si la regla requiere datos persistidos, añade la operación a la interfaz `OrderRepository` o `ProductRepository`.
3. Implementa la operación en la clase `Sqlite...Repository`.
4. Actualiza la pantalla (`OrdersView`, `HistoryView` o `ReportsView`) para mostrar la acción o el mensaje.
5. Agrega o actualiza una prueba en `src/test/java`.
6. Ejecuta las pruebas antes de distribuir la aplicación.

Evita poner reglas de negocio únicamente en botones: las reglas importantes deben comprobarse también en el servicio.

## 7. Compilar y ejecutar

Requisitos: JDK 21 y Maven (consulta `pom.xml` para las versiones configuradas).

En la carpeta raíz del proyecto:

```bash
mvn clean test
mvn package
```

Ejecuta el artefacto indicado por la configuración de Maven o inicia la clase `com.regiunic.App` desde el IDE.

En Windows también existe `mvn.cmd`, que puede ayudar a ejecutar Maven si el entorno no reconoce el comando `mvn`.

## 8. Pruebas

Las pruebas están en `src/test/java/com/regiunic/`:
- `orders/repository/SqliteOrderRepositoryTest.java`: persistencia de pedidos.
- `orders/service/OrderServiceTest.java`: validaciones y reglas de pedidos.
- `auth/repository/SqliteUserRepositoryTest.java`: persistencia de usuarios.
- `auth/service/AuthServiceTest.java`: reglas de autenticación.
- `core/config/DatabaseManagerTest.java`: inicialización de base de datos.
- `core/config/MigrationServiceTest.java`: migraciones.
- `core/concurrent/AsyncExecutorTest.java`: ejecución de tareas.
- `ui/UISanityTest.java`: comprobaciones básicas de la interfaz.

## 9. Buenas prácticas y precauciones

- Haz una copia de `data/regiunic.db` antes de modificar el esquema o ejecutar migraciones.
- Usa consultas SQL parametrizadas (`PreparedStatement`) para evitar inyección SQL.
- Cierra recursos de base de datos mediante `try-with-resources`.
- No cambies los nombres de columnas sin actualizar las migraciones y los repositorios.
- No elimines un registro histórico de forma física si la operación solicitada es solo retirar el pedido de la vista activa.
- Mantén el estilo visual centralizado en `AppTheme` y los componentes reutilizables.
- Documenta cualquier regla nueva con JavaDoc y una prueba automatizada.

## 10. Comentarios del código

Las clases Java incluyen comentarios JavaDoc en español para describir su propósito y las operaciones públicas. Los comentarios explican la intención del código; para comprender los detalles de una regla concreta, consulta primero el servicio y luego el repositorio que guarda o consulta los datos.
