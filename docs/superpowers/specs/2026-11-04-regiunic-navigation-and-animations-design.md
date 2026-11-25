# Diseño: ventana compartida y movimiento sutil

Fecha: 2026-11-04

## Intención
Mantener login, menú y pedidos dentro de un solo `JFrame`, con cambios de pantalla suaves y animación breve en botones. La navegación y las funciones actuales deben conservar sus resultados.

## Objetivos
- Reemplazar el cierre y recreación de ventanas por navegación dentro del mismo marco.
- Deslizar las pantallas al cambiar entre login, menú y pedidos; volver al menú en dirección opuesta.
- Dar a los botones una elevación pequeña al pasar el cursor y respuesta inmediata al presionarlos.
- Mantener registro como diálogo, autenticación, pedidos, cierre de sesión y controles existentes.
- Usar Swing y JDK 21, sin instalar dependencias.

## No objetivos
- Cambiar validación, almacenamiento, autenticación o permisos.
- Rediseñar pantallas o añadir movimiento continuo/decorativo.
- Modificar funcionalidades de inventario, historial, reportes o filtros que aún no estén implementadas.

## Enfoques considerados
1. **Conservar ventanas separadas:** menor refactor, pero el cambio entre secciones seguiría pareciendo un salto entre ventanas.
2. **Una ventana compartida con paneles deslizantes (seleccionado):** mantiene un marco y permite transiciones claras y cómodas; requiere reorganizar la navegación visual.
3. **Mover o fundir ventanas del sistema:** depende del soporte del sistema operativo y puede parpadear; no es fiable para esta app.

## Decisiones y límites
- Reutilizar un `JFrame` y cambiar el panel visible mediante una transición corta, implementada con Swing y sin bloquear el EDT.
- Mantener transiciones de pantalla cerca de 200 ms y movimiento de pocos píxeles; no retrasar la interacción ni la autenticación.
- Mantener el feedback actual de color y presión de botones; añadir elevación discreta en hover y evitar animaciones de texto o datos.
- Cierre de sesión vuelve al login; cerrar la ventana principal conserva el cierre de la aplicación.
- No se modifica la seguridad. Se conservan las comprobaciones de autenticación y los servicios/repositorios.

## Aprobación pendiente
Este documento describe el alcance propuesto. No se cambia código hasta que el usuario lo apruebe.
