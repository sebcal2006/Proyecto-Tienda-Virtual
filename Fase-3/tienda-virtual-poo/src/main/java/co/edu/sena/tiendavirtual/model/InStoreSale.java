package co.edu.sena.tiendavirtual.model;

import co.edu.sena.tiendavirtual.exception.BusinessRuleException;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Venta presencial o telefónica en curso (RF-019 a RF-022). No es una tabla:
 * vive en la pantalla del vendedor hasta que la confirma y se convierte en un
 * {@link Order} del canal IN_STORE. Aplica RN-002, RN-007 y RN-008.
 */
public class InStoreSale {

    private final User seller;
    private final Customer customer;
    private final Map<Product, Integer> lines = new LinkedHashMap<>();
    private Order order;                  // no es null cuando la venta está finalizada

    public InStoreSale(User seller, Customer customer) {
        Objects.requireNonNull(seller, "El vendedor es obligatorio");
        seller.requireRole(RoleName.SELLER);
        this.seller = seller;
        this.customer = Objects.requireNonNull(customer, "Cliente no encontrado");
    }

    /** RF-019 / RN-002. */
    public void addProduct(Product product, int quantity) {
        checkNotFinalized();
        if (product == null) {
            throw new BusinessRuleException("RF-018", "Producto no encontrado");
        }
        int newQuantity = lines.getOrDefault(product, 0) + quantity;
        product.checkAvailable(newQuantity);
        lines.put(product, newQuantity);
    }

    /** RF-020 / RN-007. */
    public void changeQuantity(Product product, int quantity) {
        checkNotFinalized();
        if (!lines.containsKey(product)) {
            throw new BusinessRuleException("RF-020", "El producto no está en la venta");
        }
        product.checkAvailable(quantity);
        lines.put(product, quantity);
    }

    /** RF-021 / RN-007. */
    public void removeProduct(Product product) {
        checkNotFinalized();
        lines.remove(product);
    }

    /** RF-022 / RN-008. */
    public BigDecimal getTotal() {
        return lines.entrySet().stream()
                .map(e -> e.getKey().getPrice().multiply(BigDecimal.valueOf(e.getValue())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** Finaliza la venta: crea el pedido y descuenta la existencia (RN-005). */
    public Order confirm() {
        checkNotFinalized();
        order = Order.create(customer, seller, SalesChannel.IN_STORE, lines, seller);
        return order;
    }

    public boolean isFinalized() {
        return order != null;
    }

    private void checkNotFinalized() {
        if (isFinalized()) {
            throw new BusinessRuleException("RN-007", "La venta ya fue finalizada y no se puede modificar");
        }
    }

    public User getSeller() { return seller; }
    public Customer getCustomer() { return customer; }
    public Map<Product, Integer> getLines() { return Collections.unmodifiableMap(lines); }
    public Order getOrder() { return order; }
}
