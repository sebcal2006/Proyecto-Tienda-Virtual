package co.edu.sena.tiendavirtual.model;

import java.math.BigDecimal;

/** Datos de prueba compartidos por las pruebas unitarias. */
final class TestData {

    private TestData() {
    }

    static User user(String name, RoleName role) {
        User user = new User(name, name.toLowerCase().replace(' ', '.') + "@tienda.com", "$2a$10$hash");
        user.assignRole(new Role(role, role.getLabel()));
        return user;
    }

    static Product product(String code, String price, int stock) {
        Product product = new Product(new Category("Computadores"), code, "Producto " + code, new BigDecimal(price));
        if (stock > 0) {
            InventoryMovement.entry(product, stock, user("Encargado Inventario", RoleName.INVENTORY_MANAGER), "Carga inicial");
        }
        return product;
    }

    static Customer onlineCustomer() {
        return new Customer(user("Laura Cliente", RoleName.CUSTOMER));
    }
}
