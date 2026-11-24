package co.edu.sena.tiendavirtual.model;

import co.edu.sena.tiendavirtual.exception.BusinessRuleException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CartTest {

    @Test
    void totalIsSumOfQuantityTimesPrice_RN008() {
        Cart cart = new Cart(TestData.onlineCustomer());
        cart.addProduct(TestData.product("LAP-001", "2500000", 5), 2);
        cart.addProduct(TestData.product("CEL-001", "1800000", 5), 1);
        assertEquals(new BigDecimal("6800000"), cart.getTotal());
    }

    @Test
    void addingSameProductTwiceAddsQuantity() {
        Cart cart = new Cart(TestData.onlineCustomer());
        Product laptop = TestData.product("LAP-001", "2500000", 5);
        cart.addProduct(laptop, 1);
        cart.addProduct(laptop, 2);
        assertEquals(1, cart.getItems().size());
        assertEquals(3, cart.getItems().get(0).getQuantity());
    }

    @Test
    void cannotAddProductWithoutStock_RN002() {
        Cart cart = new Cart(TestData.onlineCustomer());
        Product soldOut = TestData.product("MON-001", "900000", 0);
        BusinessRuleException e = assertThrows(BusinessRuleException.class, () -> cart.addProduct(soldOut, 1));
        assertEquals("Producto sin existencia disponible", e.getMessage());
    }

    @Test
    void cannotAddMoreThanStock_RN002() {
        Cart cart = new Cart(TestData.onlineCustomer());
        Product laptop = TestData.product("LAP-001", "2500000", 2);
        BusinessRuleException e = assertThrows(BusinessRuleException.class, () -> cart.addProduct(laptop, 3));
        assertEquals("Existencia insuficiente", e.getMessage());
    }

    @Test
    void cannotCheckoutEmptyCart_RN001() {
        Cart cart = new Cart(TestData.onlineCustomer());
        BusinessRuleException e = assertThrows(BusinessRuleException.class, cart::checkout);
        assertEquals("RN-001", e.getRule());
    }

    @Test
    void checkoutCreatesPendingOrderDiscountsStockAndEmptiesCart_RN005() {
        Cart cart = new Cart(TestData.onlineCustomer());
        Product laptop = TestData.product("LAP-001", "2500000", 5);
        cart.addProduct(laptop, 2);

        Order order = cart.checkout();

        assertEquals(OrderStatus.PENDING, order.getStatus());
        assertEquals(SalesChannel.ONLINE, order.getChannel());
        assertEquals(new BigDecimal("5000000"), order.getTotal());
        assertEquals(3, laptop.getStock());
        assertEquals(MovementType.EXIT, order.getMovements().get(0).getType());
        assertTrue(cart.isEmpty());
    }

    @Test
    void checkoutRevalidatesStock_RN002() {
        Cart cart = new Cart(TestData.onlineCustomer());
        Product laptop = TestData.product("LAP-001", "2500000", 2);
        cart.addProduct(laptop, 2);
        // otro canal vende una unidad mientras el cliente decide
        InventoryMovement.exit(laptop, 1, TestData.user("Encargado", RoleName.INVENTORY_MANAGER), "Venta en mostrador");

        assertThrows(BusinessRuleException.class, cart::checkout);
        assertEquals(1, laptop.getStock());
    }
}
