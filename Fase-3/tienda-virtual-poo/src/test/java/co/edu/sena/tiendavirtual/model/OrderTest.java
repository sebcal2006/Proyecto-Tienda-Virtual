package co.edu.sena.tiendavirtual.model;

import co.edu.sena.tiendavirtual.exception.BusinessRuleException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderTest {

    private final User seller = TestData.user("Valeria Vendedora", RoleName.SELLER);
    private final User courier = TestData.user("Andres Domiciliario", RoleName.COURIER);

    private Order paidOnlineOrder(Product product, int quantity) {
        Cart cart = new Cart(TestData.onlineCustomer());
        cart.addProduct(product, quantity);
        Order order = cart.checkout();
        order.setDeliveryAddress(new DeliveryAddress("Calle 10 # 5-32", "Neiva", null));
        order.registerPayment(new PaymentMethod("PSE")).approve();
        return order;
    }

    @Test
    void statusAdvancesInOrderUntilDelivered_RN004() {
        Order order = paidOnlineOrder(TestData.product("LAP-001", "2500000", 5), 1);
        order.advanceStatus(seller);
        Delivery delivery = Delivery.assign(order, courier, seller);
        delivery.markOnTheWay(courier);
        assertEquals(OrderStatus.ON_THE_WAY, order.getStatus());
        delivery.confirmDelivered(courier);

        assertEquals(OrderStatus.DELIVERED, order.getStatus());
        assertEquals(4, order.getStatusHistory().size());
        assertThrows(BusinessRuleException.class, () -> order.advanceStatus(seller));
    }

    @Test
    void onlineOrderNeedsApprovedPaymentToBePrepared() {
        Cart cart = new Cart(TestData.onlineCustomer());
        cart.addProduct(TestData.product("LAP-001", "2500000", 5), 1);
        Order order = cart.checkout();
        order.registerPayment(new PaymentMethod("Tarjeta")).reject();

        assertThrows(BusinessRuleException.class, () -> order.advanceStatus(seller));
        assertEquals(OrderStatus.PENDING, order.getStatus());
    }

    @Test
    void cancellingRestoresStock_RN003_RN005() {
        Product laptop = TestData.product("LAP-001", "2500000", 5);
        Order order = paidOnlineOrder(laptop, 2);
        assertEquals(3, laptop.getStock());

        order.advanceStatus(seller);          // en preparación: todavía se puede cancelar
        order.cancel(seller);

        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        assertEquals(5, laptop.getStock());
        assertEquals(MovementType.ENTRY, order.getMovements().get(1).getType());
    }

    @Test
    void cannotCancelOrderOnTheWay_RN003() {
        Product laptop = TestData.product("LAP-001", "2500000", 5);
        Order order = paidOnlineOrder(laptop, 1);
        order.advanceStatus(seller);
        Delivery.assign(order, courier, seller).markOnTheWay(courier);

        BusinessRuleException e = assertThrows(BusinessRuleException.class, () -> order.cancel(seller));
        assertEquals("RN-003", e.getRule());
        assertEquals(4, laptop.getStock());
    }

    @Test
    void priceChangeDoesNotAlterExistingOrder() {
        Product laptop = TestData.product("LAP-001", "2500000", 5);
        Order order = paidOnlineOrder(laptop, 1);
        laptop.setPrice(new BigDecimal("2700000"));
        assertEquals(new BigDecimal("2500000"), order.getTotal());
        assertEquals(new BigDecimal("2500000"), order.calculateTotal());
    }

    @Test
    void onlyAssignedCourierCanUpdateDelivery() {
        Order order = paidOnlineOrder(TestData.product("LAP-001", "2500000", 5), 1);
        order.advanceStatus(seller);
        Delivery delivery = Delivery.assign(order, courier, seller);
        User otherCourier = TestData.user("Otro Domiciliario", RoleName.COURIER);

        assertThrows(BusinessRuleException.class, () -> delivery.markOnTheWay(otherCourier));
    }

    @Test
    void cannotAssignCourierWithoutCourierRole() {
        Order order = paidOnlineOrder(TestData.product("LAP-001", "2500000", 5), 1);
        order.advanceStatus(seller);
        assertThrows(BusinessRuleException.class, () -> Delivery.assign(order, seller, seller));
    }
}
