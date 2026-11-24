package co.edu.sena.tiendavirtual;

import co.edu.sena.tiendavirtual.exception.BusinessRuleException;
import co.edu.sena.tiendavirtual.model.Cart;
import co.edu.sena.tiendavirtual.model.Category;
import co.edu.sena.tiendavirtual.model.Customer;
import co.edu.sena.tiendavirtual.model.Delivery;
import co.edu.sena.tiendavirtual.model.DeliveryAddress;
import co.edu.sena.tiendavirtual.model.InStoreSale;
import co.edu.sena.tiendavirtual.model.InventoryMovement;
import co.edu.sena.tiendavirtual.model.Invoice;
import co.edu.sena.tiendavirtual.model.Order;
import co.edu.sena.tiendavirtual.model.Payment;
import co.edu.sena.tiendavirtual.model.PaymentMethod;
import co.edu.sena.tiendavirtual.model.Product;
import co.edu.sena.tiendavirtual.model.Role;
import co.edu.sena.tiendavirtual.model.RoleName;
import co.edu.sena.tiendavirtual.model.User;

import java.math.BigDecimal;

/**
 * Recorre los dos flujos principales del sistema con datos de ejemplo
 * (los mismos productos del script SQL de la Fase 4).
 * Ejecutar con: mvn compile exec:java
 */
public class TiendaVirtualDemo {

    public static void main(String[] args) {
        // ---- Usuarios y roles (RF-002, RF-003) ----
        User admin = user("Ana Admin", "admin@tienda.com", RoleName.ADMIN);
        User seller = user("Valeria Vendedora", "vendedor@tienda.com", RoleName.SELLER);
        User stockManager = user("Iván Inventario", "inventario@tienda.com", RoleName.INVENTORY_MANAGER);
        User billing = user("Fabio Facturación", "facturacion@tienda.com", RoleName.BILLING_MANAGER);
        User courier = user("Andrés Domiciliario", "domicilios@tienda.com", RoleName.COURIER);
        User customerAccount = user("Laura Martínez", "cliente@correo.com", RoleName.CUSTOMER);

        // ---- Catálogo e inventario (RF-005, RF-011) ----
        Category computers = new Category("Computadores");
        Category phones = new Category("Celulares");
        Product laptop = new Product(computers, "LAP-001", "Laptop 14\" 16GB RAM", new BigDecimal("2500000"));
        Product phone = new Product(phones, "CEL-001", "Smartphone 128GB", new BigDecimal("1800000"));
        InventoryMovement.entry(laptop, 9, stockManager, "Compra a proveedor");
        InventoryMovement.entry(phone, 14, stockManager, "Compra a proveedor");
        title("Inventario inicial");
        System.out.println(laptop);
        System.out.println(phone);

        // ---- Canal en línea (RF-023 a RF-034) ----
        title("Compra en línea");
        Customer laura = new Customer(customerAccount);
        Cart cart = new Cart(laura);
        cart.addProduct(laptop, 1);
        cart.addProduct(phone, 2);
        System.out.println("Total del carrito: $" + cart.getTotal());
        try {
            cart.addProduct(laptop, 50);
        } catch (BusinessRuleException e) {
            System.out.println("Intento de agregar 50 laptops -> " + e.getRule() + ": " + e.getMessage());
        }
        Order online = cart.checkout();
        online.setDeliveryAddress(new DeliveryAddress("Calle 10 # 5-32, apto 201", "Neiva", "Portón verde"));
        System.out.println(online);
        System.out.println("Existencia después de la compra: " + laptop.getStock() + " laptops, "
                + phone.getStock() + " celulares (RN-005)");

        PaymentMethod pse = new PaymentMethod("PSE");
        Payment payment = online.registerPayment(pse);
        payment.approve();
        System.out.println("Pago: " + payment);

        Invoice invoice = Invoice.generate(online, billing, "FV-0001", Invoice.DEFAULT_TAX_RATE);
        System.out.println("Factura: " + invoice);
        System.out.println("¿El administrador puede consultar facturas? " + Invoice.canBeViewedBy(admin));
        System.out.println("¿El domiciliario puede consultar facturas? " + Invoice.canBeViewedBy(courier));

        online.advanceStatus(seller);                          // en preparación
        Delivery delivery = Delivery.assign(online, courier, seller);
        delivery.markOnTheWay(courier);                        // en camino (HU37)
        delivery.confirmDelivered(courier);                    // entregado
        System.out.println("Historial: " + online.getStatusHistory());

        // ---- Canal presencial (RF-015 a RF-022) ----
        title("Venta presencial");
        Customer pedro = new Customer("1075123456", "Pedro Ramírez", "3104567890");
        InStoreSale sale = new InStoreSale(seller, pedro);
        sale.addProduct(phone, 3);
        sale.changeQuantity(phone, 1);
        System.out.println("Total de la venta: $" + sale.getTotal());
        Order inStore = sale.confirm();
        System.out.println(inStore);
        try {
            sale.addProduct(laptop, 1);
        } catch (BusinessRuleException e) {
            System.out.println("Agregar después de finalizar -> " + e.getRule() + ": " + e.getMessage());
        }

        // ---- Cancelación (RN-003 y RN-005) ----
        title("Cancelación");
        inStore.cancel(seller);
        System.out.println(inStore);
        System.out.println("Existencia de celulares después de cancelar: " + phone.getStock());
        try {
            online.cancel(seller);
        } catch (BusinessRuleException e) {
            System.out.println("Cancelar un pedido entregado -> " + e.getRule() + ": " + e.getMessage());
        }
    }

    private static User user(String name, String email, RoleName role) {
        User user = new User(name, email, "$2a$10$hashDeEjemplo");
        user.assignRole(new Role(role, role.getLabel()));
        return user;
    }

    private static void title(String text) {
        System.out.println();
        System.out.println("=== " + text + " ===");
    }
}
