package co.edu.sena.tiendavirtual.model;

import co.edu.sena.tiendavirtual.exception.BusinessRuleException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InventoryAndUserTest {

    private final User manager = TestData.user("Ivan Inventario", RoleName.INVENTORY_MANAGER);

    @Test
    void newProductStartsWithZeroStock_RF005() {
        Product p = new Product(new Category("Monitores"), "mon-001", "Monitor IPS 24", new BigDecimal("900000"));
        assertEquals(0, p.getStock());
        assertEquals("MON-001", p.getCode());
    }

    @Test
    void priceCannotBeNegative_RF006() {
        Product p = TestData.product("MON-001", "900000", 0);
        assertThrows(BusinessRuleException.class, () -> p.setPrice(new BigDecimal("-1")));
    }

    @Test
    void exitGreaterThanStockIsRejected_RF012() {
        Product p = TestData.product("MON-001", "900000", 3);
        BusinessRuleException e = assertThrows(BusinessRuleException.class,
                () -> InventoryMovement.exit(p, 4, manager, "Ajuste"));
        assertEquals("Existencia insuficiente", e.getMessage());
        assertEquals(3, p.getStock());
    }

    @Test
    void movementQuantityMustBePositive_RF011() {
        Product p = TestData.product("MON-001", "900000", 0);
        assertThrows(BusinessRuleException.class, () -> InventoryMovement.entry(p, 0, manager, null));
    }

    @Test
    void onlyInventoryManagerRegistersManualMovements_RNF002() {
        Product p = TestData.product("MON-001", "900000", 0);
        User seller = TestData.user("Valeria Vendedora", RoleName.SELLER);
        assertThrows(BusinessRuleException.class, () -> InventoryMovement.entry(p, 5, seller, null));
    }

    @Test
    void deactivatedProductCannotBeSold_RF007() {
        Product p = TestData.product("MON-001", "900000", 3);
        p.deactivate();
        assertFalse(p.isAvailable(1));
    }

    @Test
    void inactiveUserCannotLogIn_RF001() {
        User user = TestData.user("Usuario Inactivo", RoleName.SELLER);
        user.deactivate();
        BusinessRuleException e = assertThrows(BusinessRuleException.class, user::checkCanLogIn);
        assertEquals("Esta cuenta se encuentra inactiva", e.getMessage());
    }

    @Test
    void userCanHaveSeveralRoles_RF003() {
        User owner = TestData.user("Dueno Tienda", RoleName.ADMIN);
        owner.assignRole(new Role(RoleName.SELLER, "Vendedor"));
        assertTrue(owner.hasRole(RoleName.ADMIN));
        assertTrue(owner.hasRole(RoleName.SELLER));
        assertEquals(2, owner.getRoles().size());
    }

    @Test
    void adminCanViewOtherModulesButNotOperateThem_RNF002() {
        User admin = TestData.user("Ana Admin", RoleName.ADMIN);
        assertTrue(admin.canView(RoleName.INVENTORY_MANAGER));
        assertThrows(BusinessRuleException.class,
                () -> InventoryMovement.entry(TestData.product("MON-001", "900000", 0), 1, admin, null));
    }
}
