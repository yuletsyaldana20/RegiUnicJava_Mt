# Refinamiento visual de RegiUnic: plan de implementación

> **Para agentes de implementación:** ejecutar cada tarea en orden y mantener el alcance visual descrito en la especificación.

**Objetivo:** unificar la tipografía y los estados visuales de las ventanas Swing, con animaciones discretas en botones.

**Arquitectura:** extraer los estilos compartidos a una clase `AppTheme` y crear un `AnimatedButton` reusable. `RegiUnicApp` conservará sus ventanas, listeners y lógica de negocio; solo delegará los estilos de UI a esos componentes.

**Tecnologías:** Java 21, Swing y Maven existentes; sin dependencias nuevas.

**Diseño:** `docs/superpowers/specs/2026-10-30-interfaz-moderna-design.md`.

## Restricciones globales

- Mantener Java 21, Swing y Maven; no agregar dependencias.
- No cambiar autenticación, registro, persistencia, navegación ni pedidos.
- Hover: transición de color de 140 ms; pulsación con mouse: escala 0.98.
- Mantener foco de teclado visible y botones operables por teclado.
- No agregar movimiento de entrada ni animar las acciones de teclado.

## Revisión de casos límite

- Botón deshabilitado: detener la animación y mostrar estado inactivo.
- Mouse sale durante la transición: invertir hacia el color base desde el estado actual.
- Activación con teclado: conservar el comportamiento actual y no aplicar escala de mouse.
- Botón de cerrar sesión: usar su paleta roja sin perder contraste.
- Diálogos y tabla: mantener sus controles y lectura con la tipografía compartida.

## Archivos

- Crear `src/main/java/com/regiunic/AppTheme.java`: paleta, tipografía y propiedades globales de Swing.
- Crear `src/main/java/com/regiunic/AnimatedButton.java`: botón con transición de color y escala de pulsación por mouse.
- Modificar `src/main/java/com/regiunic/RegiUnicApp.java`: aplicar el tema, usar los estilos tipográficos y botones compartidos; preservar los listeners existentes.

## Tareas

### Tarea 1: tema y jerarquía tipográfica

- [x] Definir colores de la interfaz existente y roles de fuente Segoe UI: cuerpo 14 px, etiqueta 15 px, sección 22 px y título 32 px.
- [x] Instalar valores predeterminados de Swing antes de construir la primera ventana.
- [x] Cambiar fuentes literales en `RegiUnicApp` por roles compartidos, manteniendo pesos y jerarquía.

### Tarea 2: botón reusable con movimiento acotado

- [x] Aceptar texto y colores normal, hover y pulsado; mantener ActionListener/Action actual.
- [x] Interpolar solo el color de fondo durante 140 ms con `javax.swing.Timer`; cancelar o invertir la transición si llega otro evento.
- [x] Aplicar escala 0.98 solo al presionar con mouse y pintar el fondo con antialiasing.
- [x] Pintar foco de teclado visible; no transformar al activar por teclado ni animar si está deshabilitado.

### Tarea 3: aplicar controles en cada ventana

- [x] Aplicar el botón reutilizable a login, registro, menú, cerrar sesión, volver al menú y acciones de pedidos.
- [ ] Conservar colores azules; mantener rojo para cerrar sesión.
- [x] Preservar listeners y llamadas existentes a autenticación, registro, navegación y pedidos.
- [x] Mantener tipografía compartida en etiquetas, campos, tablas y diálogos.

### Tarea 4: verificación

- [x] Compilar sin ejecutar pruebas automatizadas: `mvn -DskipTests compile`.
- [x] Revisar visualmente el login. Pendiente revisar el resto de ventanas y estados de interacción; la ventana detectó actividad del usuario y no continué interactuando.
- [x] Revisar que los listeners de autenticación, registro, navegación y pedidos permanecen conectados.
