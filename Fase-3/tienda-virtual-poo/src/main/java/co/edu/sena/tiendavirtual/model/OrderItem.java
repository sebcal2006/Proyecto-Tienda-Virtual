package co.edu.sena.tiendavirtual.model;

import java.math.BigDecimal;

/**
 * Línea de un pedido (tabla order_items). Guarda el precio unitario del momento
 * de la venta para que un cambio de precio posterior no altere el pedido.
 */
public class OrderItem {

    private Integer id;
    private final Product product;
    private final int quantity;
    private final BigDecimal unitPrice;

    OrderItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
        this.unitPrice = product.getPrice();
    }

    /** RN-008: cantidad x precio unitario. */
    public BigDecimal getSubtotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Product getProduct() { return product; }
    public int getQuantity() { return quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }

    @Override
    public String toString() {
        return quantity + " x " + product.getName() + " = $" + getSubtotal();
    }
}
