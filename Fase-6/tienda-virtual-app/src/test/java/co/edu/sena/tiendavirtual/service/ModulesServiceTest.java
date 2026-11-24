package co.edu.sena.tiendavirtual.service;

import co.edu.sena.tiendavirtual.entity.*;
import co.edu.sena.tiendavirtual.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Reglas de los módulos de administración, inventario y facturación (sin base de datos). */
class ModulesServiceTest {

    // ------------------------------------------------------------------ inventario

    private InventoryService inventory(Product product) {
        ProductRepository products = mock(ProductRepository.class);
        InventoryMovementRepository movements = mock(InventoryMovementRepository.class);
        when(products.findByIdForUpdate(any())).thenReturn(Optional.of(product));
        when(movements.save(any())).thenAnswer(inv -> inv.getArgument(0));
        return new InventoryService(products, movements);
    }

    @Test
    void rf011EntradaSumaExistencia() {
        Product p = new Product("ACC-001", "Mouse", new BigDecimal("45000"), 5);
        inventory(p).registerEntry(1, 10, null, new User());
        assertEquals(15, p.getStock());
    }

    @Test
    void rf011EntradaConCantidadCeroSeRechaza() {
        Product p = new Product("ACC-001", "Mouse", new BigDecimal("45000"), 5);
        assertThrows(BusinessException.class, () -> inventory(p).registerEntry(1, 0, null, new User()));
        assertEquals(5, p.getStock());
    }

    @Test
    void rf012SalidaMayorQueExistenciaSeRechaza() {
        Product p = new Product("ACC-001", "Mouse", new BigDecimal("45000"), 5);
        BusinessException ex = assertThrows(BusinessException.class,
                () -> inventory(p).registerExit(1, 6, null, new User()));
        assertTrue(ex.getMessage().startsWith("Existencia insuficiente"));
        inventory(p).registerExit(1, 5, null, new User());
        assertEquals(0, p.getStock());
    }

    // ------------------------------------------------------------------ productos

    private AdminService admin(ProductRepository products, CategoryRepository categories) {
        return new AdminService(mock(UserRepository.class), mock(RoleRepository.class),
                mock(CustomerRepository.class), products, categories, mock(PasswordEncoder.class));
    }

    @Test
    void rf005CodigoRepetidoSeRechaza() {
        ProductRepository products = mock(ProductRepository.class);
        when(products.existsByCodeIgnoreCase("ACC-001")).thenReturn(true);
        BusinessException ex = assertThrows(BusinessException.class, () -> admin(products, mock(CategoryRepository.class))
                .createProduct(new AdminService.ProductData("ACC-001", "Mouse", null, BigDecimal.TEN, 1)));
        assertEquals("Ya existe un producto con este código", ex.getMessage());
    }

    @Test
    void rf005ProductoNuevoEntraConExistenciaCero() {
        ProductRepository products = mock(ProductRepository.class);
        CategoryRepository categories = mock(CategoryRepository.class);
        when(categories.findById(1)).thenReturn(Optional.of(mock(Category.class)));
        when(products.save(any())).thenAnswer(inv -> inv.getArgument(0));
        Product p = admin(products, categories)
                .createProduct(new AdminService.ProductData("acc-010", "Cable", null, new BigDecimal("15000"), 1));
        assertEquals(0, p.getStock());
        assertEquals("ACC-010", p.getCode());
    }

    @Test
    void rf006PrecioNegativoSeRechaza() {
        ProductRepository products = mock(ProductRepository.class);
        when(products.findById(1)).thenReturn(Optional.of(new Product("ACC-001", "Mouse", BigDecimal.TEN, 5)));
        BusinessException ex = assertThrows(BusinessException.class, () -> admin(products, mock(CategoryRepository.class))
                .updateProduct(1, new AdminService.ProductData("ACC-001", "Mouse", null, new BigDecimal("-1"), 1)));
        assertEquals("El precio no puede ser negativo", ex.getMessage());
    }

    @Test
    void rf003RolInexistenteSeRechaza() {
        UserRepository users = mock(UserRepository.class);
        RoleRepository roles = mock(RoleRepository.class);
        when(roles.findById(99)).thenReturn(Optional.empty());
        AdminService service = new AdminService(users, roles, mock(CustomerRepository.class),
                mock(ProductRepository.class), mock(CategoryRepository.class), mock(PasswordEncoder.class));
        BusinessException ex = assertThrows(BusinessException.class, () -> service.createUser(
                new AdminService.UserData("Pedro", "pedro@tienda.com", "Clave1234", null, true, List.of(99))));
        assertEquals("El rol seleccionado no es válido", ex.getMessage());
    }

    // ------------------------------------------------------------------ facturación

    @Test
    void rf041NoSeDuplicaLaFactura() {
        InvoiceRepository invoices = mock(InvoiceRepository.class);
        OrderRepository orders = mock(OrderRepository.class);
        Order order = mock(Order.class);
        when(order.getCode()).thenReturn("PED-0001");
        when(orders.findById(1)).thenReturn(Optional.of(order));
        Invoice existing = mock(Invoice.class);
        when(existing.getNumber()).thenReturn("FV-0001");
        when(invoices.findByOrder(order)).thenReturn(Optional.of(existing));
        BusinessException ex = assertThrows(BusinessException.class,
                () -> new InvoiceService(invoices, orders).generate(1, new User()));
        assertTrue(ex.getMessage().contains("ya tiene la factura FV-0001"));
        verify(invoices, never()).save(any());
    }

    @Test
    void rf041NumeroConsecutivoEIva() {
        InvoiceRepository invoices = mock(InvoiceRepository.class);
        OrderRepository orders = mock(OrderRepository.class);
        Invoice existing = mock(Invoice.class);
        when(existing.getNumber()).thenReturn("FV-0003");
        when(invoices.findAll()).thenReturn(List.of(existing));
        Order order = mock(Order.class);
        when(order.isBillable()).thenReturn(true);
        when(order.getTotal()).thenReturn(new BigDecimal("100000.00"));
        when(orders.findById(5)).thenReturn(Optional.of(order));
        when(invoices.findByOrder(order)).thenReturn(Optional.empty());
        when(invoices.save(any())).thenAnswer(inv -> inv.getArgument(0));
        Invoice invoice = new InvoiceService(invoices, orders).generate(5, new User());
        assertEquals("FV-0004", invoice.getNumber());
        assertEquals(0, new BigDecimal("19000.00").compareTo(invoice.getTax()));
        assertEquals(0, new BigDecimal("119000.00").compareTo(invoice.getTotal()));
    }
}
