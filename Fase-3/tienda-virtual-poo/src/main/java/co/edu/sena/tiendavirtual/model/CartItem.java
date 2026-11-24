package co.edu.sena.tiendavirtual.model;

import java.math.BigDecimal;

/** Producto dentro del carrito (tabla cart_items). */
public class CartItem {

    private Integer id;
    private final Product product;
    private int quantity;

    CartItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    /** RN-008. */
    public BigDecimal getSubtotal() {
        return product.getPrice().multiply(BigDecimal.valueOf(quantity));
    }

    void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Product getProduct() { return product; }
    public int getQuantity() { return quantity; }

    @Override
    public String toString() {
        return quantity + " x " + product.getName() + " = $" + getSubtotal();
    }
}
