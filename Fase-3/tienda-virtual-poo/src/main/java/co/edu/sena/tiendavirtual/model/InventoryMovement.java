package co.edu.sena.tiendavirtual.model;

import co.edu.sena.tiendavirtual.exception.BusinessRuleException;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entrada o salida de mercancía (tabla inventory_movements). RF-011, RF-012, RF-014.
 * Crear un movimiento actualiza la existencia del producto.
 * Los movimientos ligados a un pedido aplican la RN-005.
 */
public class InventoryMovement {

    private Integer id;
    private final Product product;
    private final User registeredBy;
    private final Order order;            // null si es un movimiento manual
    private final MovementType type;
    private final int quantity;
    private final String notes;
    private final LocalDateTime createdAt = LocalDateTime.now();

    private InventoryMovement(Product product, User registeredBy, Order order,
                              MovementType type, int quantity, String notes) {
        this.product = Objects.requireNonNull(product, "Producto no encontrado");
        this.registeredBy = Objects.requireNonNull(registeredBy, "El usuario es obligatorio");
        if (quantity <= 0) {
            throw new BusinessRuleException("RF-011", "La cantidad debe ser mayor que cero");
        }
        this.order = order;
        this.type = type;
        this.quantity = quantity;
        this.notes = notes;
        if (type == MovementType.ENTRY) {
            product.increaseStock(quantity);
        } else {
            product.decreaseStock(quantity);
        }
    }

    /** RF-011: entrada manual registrada por el encargado de inventario. */
    public static InventoryMovement entry(Product product, int quantity, User manager, String notes) {
        manager.requireRole(RoleName.INVENTORY_MANAGER);
        return new InventoryMovement(product, manager, null, MovementType.ENTRY, quantity, notes);
    }

    /** RF-012: salida manual registrada por el encargado de inventario. */
    public static InventoryMovement exit(Product product, int quantity, User manager, String notes) {
        manager.requireRole(RoleName.INVENTORY_MANAGER);
        return new InventoryMovement(product, manager, null, MovementType.EXIT, quantity, notes);
    }

    /** RN-005: salida automática al confirmar una venta o compra. */
    static InventoryMovement saleExit(Product product, int quantity, User by, Order order) {
        return new InventoryMovement(product, by, order, MovementType.EXIT, quantity,
                "Salida por pedido");
    }

    /** RN-005: devolución automática al cancelar un pedido. */
    static InventoryMovement cancellationEntry(Product product, int quantity, User by, Order order) {
        return new InventoryMovement(product, by, order, MovementType.ENTRY, quantity,
                "Devolución por cancelación del pedido");
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Product getProduct() { return product; }
    public User getRegisteredBy() { return registeredBy; }
    public Order getOrder() { return order; }
    public MovementType getType() { return type; }
    public int getQuantity() { return quantity; }
    public String getNotes() { return notes; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    @Override
    public String toString() {
        return type + " " + quantity + " x " + product.getCode() + (notes != null ? " (" + notes + ")" : "");
    }
}
