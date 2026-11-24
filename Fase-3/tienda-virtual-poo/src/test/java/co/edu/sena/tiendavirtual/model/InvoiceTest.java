package co.edu.sena.tiendavirtual.model;

import co.edu.sena.tiendavirtual.exception.BusinessRuleException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InvoiceTest {

    private final User billing = TestData.user("Fabio Facturacion", RoleName.BILLING_MANAGER);

    private Order onlineOrder() {
        Cart cart = new Cart(TestData.onlineCustomer());
        cart.addProduct(TestData.product("LAP-001", "2590000", 5), 1);
        return cart.checkout();
    }

    @Test
    void cannotInvoiceOnlineOrderWithoutApprovedPayment_RN006() {
        Order order = onlineOrder();
        BusinessRuleException e = assertThrows(BusinessRuleException.class,
                () -> Invoice.generate(order, billing, "FV-0001", Invoice.DEFAULT_TAX_RATE));
        assertEquals("RN-006", e.getRule());
    }

    @Test
    void totalIsSubtotalPlusTax() {
        Order order = onlineOrder();
        order.registerPayment(new PaymentMethod("PSE")).approve();

        Invoice invoice = Invoice.generate(order, billing, "FV-0001", Invoice.DEFAULT_TAX_RATE);

        // mismos valores que la factura FV-0001 del script SQL
        assertEquals(new BigDecimal("2590000"), invoice.getSubtotal());
        assertEquals(new BigDecimal("492100.00"), invoice.getTax());
        assertEquals(new BigDecimal("3082100.00"), invoice.getTotal());
    }

    @Test
    void onlyOneInvoicePerOrder_RF041() {
        Order order = onlineOrder();
        order.registerPayment(new PaymentMethod("PSE")).approve();
        Invoice.generate(order, billing, "FV-0001", Invoice.DEFAULT_TAX_RATE);
        assertThrows(BusinessRuleException.class,
                () -> Invoice.generate(order, billing, "FV-0002", Invoice.DEFAULT_TAX_RATE));
    }

    @Test
    void inStoreSaleCanBeInvoicedOnceConfirmed_RN006() {
        User seller = TestData.user("Valeria Vendedora", RoleName.SELLER);
        InStoreSale sale = new InStoreSale(seller, new Customer("123", "Pedro", null));
        sale.addProduct(TestData.product("CEL-001", "165000", 3), 1);
        Invoice invoice = Invoice.generate(sale.confirm(), billing, "FV-0002", Invoice.DEFAULT_TAX_RATE);
        assertEquals(new BigDecimal("196350.00"), invoice.getTotal());
    }

    @Test
    void adminAndBillingManagerCanViewInvoices_RF042() {
        assertTrue(Invoice.canBeViewedBy(billing));
        assertTrue(Invoice.canBeViewedBy(TestData.user("Ana Admin", RoleName.ADMIN)));
        assertFalse(Invoice.canBeViewedBy(TestData.user("Valeria Vendedora", RoleName.SELLER)));
    }
}
