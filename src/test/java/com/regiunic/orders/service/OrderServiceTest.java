package com.regiunic.orders.service;

import com.regiunic.orders.model.Order;
import com.regiunic.orders.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas Unitarias de Reglas de Negocio de Órdenes (OrderService)")
class OrderServiceTest {

    @Mock
    private OrderRepository repository;

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(repository);
    }

    @Test
    @DisplayName("Debe registrar una orden válida con estado 'Pendiente' y valores formateados")
    void testRegisterValidOrder() throws SQLException {
        Order registered = orderService.register("Empresa XYZ", "Impresora Láser", "3", "450.50", "Calle Central #50");

        assertThat(registered).isNotNull();
        assertThat(registered.client()).isEqualTo("Empresa XYZ");
        assertThat(registered.product()).isEqualTo("Impresora Láser");
        assertThat(registered.quantity()).isEqualTo(3);
        assertThat(registered.unitPrice()).isEqualTo(450.50);
        assertThat(registered.total()).isEqualTo(1351.50);
        assertThat(registered.status()).isEqualTo("Pendiente");

        verify(repository).save(any(Order.class));
    }

    @Test
    @DisplayName("Debe aceptar precios formateados con coma decimal (ej. '450,50')")
    void testRegisterPriceWithComma() throws SQLException {
        Order registered = orderService.register("Cliente Coma", "Monitor", "1", "1200,99", "Dirección");
        assertThat(registered.unitPrice()).isEqualTo(1200.99);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    @DisplayName("Debe rechazar registros con campos obligatorios en blanco")
    void testRegisterWithBlankFields(String blank) {
        assertThatThrownBy(() -> orderService.register(blank, "Producto", "1", "100", "Direccion"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Completa cliente, producto y dirección");

        assertThatThrownBy(() -> orderService.register("Cliente", blank, "1", "100", "Direccion"))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> orderService.register("Cliente", "Producto", "1", "100", blank))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Debe rechazar registros con cantidades menores a 1")
    void testRegisterWithNegativeOrZeroQuantity() {
        assertThatThrownBy(() -> orderService.register("Cliente", "Producto", "0", "100", "Direccion"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("mayor que cero");

        assertThatThrownBy(() -> orderService.register("Cliente", "Producto", "-5", "100", "Direccion"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Debe rechazar registros con precios menores o iguales a cero")
    void testRegisterWithZeroOrNegativePrice() {
        assertThatThrownBy(() -> orderService.register("Cliente", "Producto", "1", "0", "Direccion"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("mayor que cero");

        assertThatThrownBy(() -> orderService.register("Cliente", "Producto", "1", "-15.5", "Direccion"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Debe rechazar registros con cantidad o precio no numéricos")
    void testRegisterWithNonNumericValues() {
        assertThatThrownBy(() -> orderService.register("Cliente", "Producto", "tres", "100", "Direccion"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("números válidos");

        assertThatThrownBy(() -> orderService.register("Cliente", "Producto", "1", "gratis", "Direccion"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Debe actualizar una orden existente con estado permitido")
    void testUpdateOrderValid() throws SQLException {
        when(repository.findById("ord-99")).thenReturn(Optional.of(
                new Order("ord-99", "Cliente", "Prod", 1, 100.0, "Dir", "Pendiente", LocalDateTime.now())
        ));

        orderService.updateOrder("ord-99", "Cliente Renovado", "Prod Plus", "5", "120.00", "Nueva Dir", "En Proceso");

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(repository).update(captor.capture());

        Order updated = captor.getValue();
        assertThat(updated.id()).isEqualTo("ord-99");
        assertThat(updated.client()).isEqualTo("Cliente Renovado");
        assertThat(updated.product()).isEqualTo("Prod Plus");
        assertThat(updated.quantity()).isEqualTo(5);
        assertThat(updated.unitPrice()).isEqualTo(120.00);
        assertThat(updated.status()).isEqualTo("En Proceso");
    }

    @Test
    @DisplayName("Debe rechazar actualización con estados inválidos")
    void testUpdateOrderWithInvalidStatus() throws SQLException {
        assertThatThrownBy(() -> orderService.updateOrder("ord-99", "Cliente", "Prod", "1", "100", "Dir", "EstadoInexistente"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Estado inválido");

        verify(repository, never()).update(any());
    }

    @Test
    @DisplayName("Flujo de listado, obtención por ID y eliminación")
    void testListGetAndDelete() throws SQLException {
        Order o1 = new Order("1", "C1", "P1", 1, 10.0, "D1", "Pendiente", LocalDateTime.now());
        when(repository.findAll()).thenReturn(List.of(o1));
        when(repository.findById("1")).thenReturn(Optional.of(o1));
        when(repository.delete("1")).thenReturn(true);

        List<Order> list = orderService.listOrders();
        assertThat(list).containsExactly(o1);

        Optional<Order> fetched = orderService.getOrder("1");
        assertThat(fetched).isPresent().contains(o1);

        boolean deleted = orderService.deleteOrder("1");
        assertThat(deleted).isTrue();
        verify(repository).delete("1");
    }
}
