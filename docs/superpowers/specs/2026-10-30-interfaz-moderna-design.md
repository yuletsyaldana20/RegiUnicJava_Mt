# Diseño: refinamiento visual de RegiUnic

## Intención

Hacer que el login, el registro, el menú y la ventana de pedidos se vean coherentes, actuales y fáciles de usar. Conservar los flujos y resultados actuales.

## Objetivos

- Unificar tipografía, tamaños por jerarquía, colores y estados de controles.
- Dar respuesta visual breve a los botones con hover y pulsación, sin movimiento ornamental.
- Mantener accesible el foco de teclado y el uso con teclado.
- Separar los estilos reutilizables de la lógica de `RegiUnicApp`.

## Fuera de alcance

- Cambiar autenticación, registro, almacenamiento CSV, pedidos, navegación o contenido.
- Reemplazar Swing, agregar dependencias o rediseñar el flujo entre ventanas.
- Animar acciones iniciadas por teclado o agregar animaciones de entrada a las pantallas.

## Enfoques considerados

| Enfoque | Ventaja | Costo o riesgo |
| --- | --- | --- |
| Conservar Swing y centralizar estilos/controles compartidos | Cambio acotado, sin dependencias, mantiene los flujos | La animación se limita a estados simples de botones |
| Agregar FlatLaf | Apariencia Swing moderna lista para usar | Nueva dependencia y cambios más amplios en todos los controles |
| Migrar a JavaFX | Mayor libertad para animaciones y layouts | Reescritura extensa con alto riesgo para el funcionamiento |

## Enfoque elegido

Conservar Swing y centralizar una escala tipográfica Segoe UI, la paleta azul y clara existente, los estilos globales de controles y un botón reutilizable. Añadir transiciones de color de 140 ms para hover y escala inmediata muy sutil al presionar con mouse. No animar navegación, acciones de teclado ni carga de ventanas.

## Restricciones

- Java 21 y estructura Maven existente.
- No alterar listeners de negocio ni firmas de los servicios/repositorios.
- Deshabilitar la animación cuando el botón esté deshabilitado; conservar un foco de teclado visible.
- Sin bibliotecas nuevas.

## Criterios de aceptación

- Login, diálogo de registro, menú y pedidos comparten tipografía, paleta y estados de botones.
- El hover se interpola en 140 ms; la pulsación de mouse reduce el botón a 0.98 mientras está presionado.
- El teclado puede enfocar y activar todos los botones sin depender de animación.
- Navegación, autenticación, registro y pedidos mantienen el mismo código de acción.
- El proyecto compila con Maven y una revisión visual confirma que los controles siguen legibles.
