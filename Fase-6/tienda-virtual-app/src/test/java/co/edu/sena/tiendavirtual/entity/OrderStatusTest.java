package co.edu.sena.tiendavirtual.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** RN-003 y RN-004. */
class OrderStatusTest {

    @Test
    void sigueElOrdenPendientePreparacionCaminoEntregado() {
        assertEquals(OrderStatus.IN_PREPARATION, OrderStatus.PENDING.next());
        assertEquals(OrderStatus.ON_THE_WAY, OrderStatus.IN_PREPARATION.next());
        assertEquals(OrderStatus.DELIVERED, OrderStatus.ON_THE_WAY.next());
        assertNull(OrderStatus.DELIVERED.next());
        assertNull(OrderStatus.CANCELLED.next());
    }

    @Test
    void soloSeCancelaPendienteOEnPreparacion() {
        assertTrue(OrderStatus.PENDING.canBeCancelled());
        assertTrue(OrderStatus.IN_PREPARATION.canBeCancelled());
        assertFalse(OrderStatus.ON_THE_WAY.canBeCancelled());
        assertFalse(OrderStatus.DELIVERED.canBeCancelled());
        assertFalse(OrderStatus.CANCELLED.canBeCancelled());
    }
}
