package co.edu.sena.tiendavirtual.model;

import co.edu.sena.tiendavirtual.exception.BusinessRuleException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InStoreSaleTest {

    private final User seller = TestData.user("Valeria Vendedora", RoleName.SELLER);
    private final Customer pedro = new Customer("1075123456", "Pedro Ramirez", "3104567890");

    @Test
    void onlySellerCanStartSale_RNF002() {
        User admin = TestData.user("Ana Admin", RoleName.ADMIN);
        assertThrows(BusinessRuleException.class, () -> new InStoreSale(admin, pedro));
    }

    @Test
    void sellerCanEditLinesBeforeConfirming_RF020_RF021() {
        Product phone = TestData.product("CEL-001", "1800000", 10);
        Product laptop = TestData.product("LAP-001", "2500000", 10);
        InStoreSale sale = new InStoreSale(seller, pedro);
        sale.addProduct(phone, 3);
        sale.addProduct(laptop, 1);
        sale.changeQuantity(phone, 1);
        sale.removeProduct(laptop);

        assertEquals(new BigDecimal("1800000"), sale.getTotal());
        assertEquals(10, phone.getStock());   // aún no se descuenta
    }

    @Test
    void confirmCreatesInStoreOrderAndDiscountsStock_RN005() {
        Product phone = TestData.product("CEL-001", "1800000", 10);
        InStoreSale sale = new InStoreSale(seller, pedro);
        sale.addProduct(phone, 2);

        Order order = sale.confirm();

        assertEquals(SalesChannel.IN_STORE, order.getChannel());
        assertEquals(seller, order.getSeller());
        assertEquals(8, phone.getStock());
        assertTrue(sale.isFinalized());
    }

    @Test
    void cannotEditAfterConfirming_RN007() {
        Product phone = TestData.product("CEL-001", "1800000", 10);
        InStoreSale sale = new InStoreSale(seller, pedro);
        sale.addProduct(phone, 1);
        sale.confirm();

        BusinessRuleException e = assertThrows(BusinessRuleException.class, () -> sale.removeProduct(phone));
        assertEquals("RN-007", e.getRule());
    }

    @Test
    void customerWithoutAccountHasNoUser_RF015() {
        assertNull(pedro.getUser());
    }
}
