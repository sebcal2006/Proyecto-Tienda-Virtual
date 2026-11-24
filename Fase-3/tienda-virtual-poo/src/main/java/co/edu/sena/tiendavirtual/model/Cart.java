package co.edu.sena.tiendavirtual.model;

import co.edu.sena.tiendavirtual.exception.BusinessRuleException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Carrito de compras del cliente (tabla carts). RF-023 a RF-026.
 * Aplica RN-001, RN-002 y RN-008.
 */
public class Cart {

    private Integer id;
    private final Customer customer;
    private final List<CartItem> items = new ArrayList<>();

    public Cart(Customer customer) {
        if (customer == null || !customer.hasAccount()) {
            throw new BusinessRuleException("RF-023", "Debe iniciar sesión como cliente para usar el carrito");
        }
        this.customer = customer;
    }

    /** RF-023 / RN-002: si el producto ya está en el carrito, suma la cantidad. */
    public void addProduct(Product product, int quantity) {
        if (product == null) {
            throw new BusinessRuleException("RF-023", "Producto no encontrado");
        }
        Optional<CartItem> existing = find(product);
        int newQuantity = quantity + existing.map(CartItem::getQuantity).orElse(0);
        product.checkAvailable(newQuantity);
        if (existing.isPresent()) {
            existing.get().setQuantity(newQuantity);
        } else {
            items.add(new CartItem(product, quantity));
        }
    }

    /** RF-024: cambia la cantidad; con 0 o menos elimina el producto. */
    public void updateQuantity(Product product, int quantity) {
        CartItem item = find(product).orElseThrow(
                () -> new BusinessRuleException("RF-024", "El producto no está en el carrito"));
        if (quantity <= 0) {
            items.remove(item);
            return;
        }
        product.checkAvailable(quantity);
        item.setQuantity(quantity);
    }

    /** RF-024. */
    public void removeProduct(Product product) {
        items.removeIf(i -> i.getProduct() == product);
    }

    /** RF-025 / RN-008. */
    public BigDecimal getTotal() {
        return items.stream().map(CartItem::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    /**
     * RF-026: confirma la compra. RN-001 (carrito no vacío), RN-002 (vuelve a validar
     * existencias) y RN-005 (descuenta inventario). Al terminar, el carrito queda vacío.
     */
    public Order checkout() {
        if (isEmpty()) {
            throw new BusinessRuleException("RN-001", "El carrito está vacío");
        }
        Map<Product, Integer> lines = new LinkedHashMap<>();
        items.forEach(i -> lines.put(i.getProduct(), i.getQuantity()));
        Order order = Order.create(customer, null, SalesChannel.ONLINE, lines, customer.getUser());
        items.clear();
        return order;
    }

    private Optional<CartItem> find(Product product) {
        return items.stream().filter(i -> i.getProduct() == product).findFirst();
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Customer getCustomer() { return customer; }
    public List<CartItem> getItems() { return Collections.unmodifiableList(items); }
}
