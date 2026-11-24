package co.edu.sena.tiendavirtual.service;

import co.edu.sena.tiendavirtual.entity.*;
import co.edu.sena.tiendavirtual.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Reglas de negocio del pedido con repositorios simulados (sin base de datos). */
class OrderServiceTest {

    private OrderRepository orders;
    private ProductRepository products;
    private InventoryMovementRepository movements;
    private CartRepository carts;
    private CustomerService customers;
    private OrderService service;

    private final Customer customer = new Customer();
    private final User seller = new User();
    private Product laptop;

    @BeforeEach
    void setUp() {
        orders = mock(OrderRepository.class);
        products = mock(ProductRepository.class);
        movements = mock(InventoryMovementRepository.class);
        carts = mock(CartRepository.class);
        customers = mock(CustomerService.class);
        service = new OrderService(orders, products, movements, carts,
                mock(DeliveryAddressRepository.class), mock(PaymentMethodRepository.class), customers,
                mock(DeliveryRepository.class));

        laptop = new Product("P-001", "Portátil", new BigDecimal("2500000"), 5);
        when(products.findByIdForUpdate(any())).thenReturn(Optional.of(laptop));
        when(orders.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(customers.get(1)).thenReturn(customer);
    }

    private Order sell(int quantity) {
        SaleDraft draft = new SaleDraft();
        draft.setCustomerId(1);
        draft.getLines().put(10, quantity);
        Order order = service.confirmInStoreSale(draft, seller);
        when(orders.findById(7)).thenReturn(Optional.of(order));
        return order;
    }

    @Test
    void rn001NoSeConfirmaConCarritoVacio() {
        when(carts.findByCustomer(customer)).thenReturn(Optional.of(new Cart(customer)));
        BusinessException ex = assertThrows(BusinessException.class, () -> service.checkout(customer, seller,
                new OrderService.CheckoutData("Calle 1", "Bogotá", null, 1, false)));
        assertEquals("El carrito está vacío", ex.getMessage());
    }

    @Test
    void rn005YRn008VentaDescuentaInventarioYCalculaTotal() {
        Order order = sell(2);
        assertEquals(3, laptop.getStock());
        assertEquals(0, new BigDecimal("5000000").compareTo(order.getTotal()));
        assertEquals(OrderStatus.PENDING, order.getStatus());
        verify(movements, times(1)).save(any());
    }

    @Test
    void rn002NoSeVendeMasDeLaExistencia() {
        BusinessException ex = assertThrows(BusinessException.class, () -> sell(6));
        assertTrue(ex.getMessage().startsWith("Existencia insuficiente"));
        assertEquals(5, laptop.getStock());
    }

    @Test
    void rn005CancelarDevuelveLasUnidades() {
        sell(2);
        Order cancelled = service.cancel(7, seller);
        assertEquals(OrderStatus.CANCELLED, cancelled.getStatus());
        assertEquals(5, laptop.getStock());
    }

    @Test
    void rn003NoSeCancelaUnPedidoEntregado() {
        sell(1);
        service.advance(7, seller);
        service.advance(7, seller);
        service.advance(7, seller);
        BusinessException ex = assertThrows(BusinessException.class, () -> service.cancel(7, seller));
        assertEquals("No se puede cancelar un pedido entregado", ex.getMessage());
    }

    @Test
    void rn004ElEstadoAvanzaEnOrden() {
        Order order = sell(1);
        service.advance(7, seller);
        assertEquals(OrderStatus.IN_PREPARATION, order.getStatus());
        assertEquals(2, order.getHistory().size());
    }
}
