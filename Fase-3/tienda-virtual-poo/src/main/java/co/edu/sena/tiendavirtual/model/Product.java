package co.edu.sena.tiendavirtual.model;

import co.edu.sena.tiendavirtual.exception.BusinessRuleException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Producto del catálogo (tabla products). RF-005 a RF-007.
 * La existencia solo cambia mediante {@link InventoryMovement} para que
 * cada cambio quede en el historial (RF-014).
 */
public class Product {

    private Integer id;
    private Category category;
    private final String code;
    private String name;
    private String description;
    private BigDecimal price;
    private int stock;                // RF-005: inicia en 0
    private boolean active = true;
    private final LocalDateTime createdAt = LocalDateTime.now();

    public Product(Category category, String code, String name, BigDecimal price) {
        if (code == null || code.isBlank()) {
            throw new BusinessRuleException("RF-005", "El código del producto es obligatorio");
        }
        this.code = code.trim().toUpperCase();
        setCategory(category);
        setName(name);
        setPrice(price);
    }

    /** RN-002: no se vende un producto inactivo o sin existencia suficiente. */
    public boolean isAvailable(int quantity) {
        return active && quantity > 0 && stock >= quantity;
    }

    /** Valida RN-002 y lanza el mensaje del criterio de aceptación. */
    public void checkAvailable(int quantity) {
        if (quantity <= 0) {
            throw new BusinessRuleException("RN-008", "La cantidad debe ser mayor que cero");
        }
        if (!active || stock == 0) {
            throw new BusinessRuleException("RN-002", "Producto sin existencia disponible");
        }
        if (stock < quantity) {
            throw new BusinessRuleException("RN-002", "Existencia insuficiente");
        }
    }

    /** Usado solo por InventoryMovement. */
    void increaseStock(int quantity) {
        stock += quantity;
    }

    /** Usado solo por InventoryMovement. RF-012 E1. */
    void decreaseStock(int quantity) {
        if (stock < quantity) {
            throw new BusinessRuleException("RF-012", "Existencia insuficiente");
        }
        stock -= quantity;
    }

    /** RF-007: se desactiva, no se borra. */
    public void deactivate() { this.active = false; }
    public void activate() { this.active = true; }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Category getCategory() { return category; }
    public void setCategory(Category category) {
        this.category = Objects.requireNonNull(category, "La categoría es obligatoria");
    }
    public String getCode() { return code; }
    public String getName() { return name; }

    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new BusinessRuleException("RF-005", "El nombre del producto es obligatorio");
        }
        this.name = name.trim();
    }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getPrice() { return price; }

    /** CA-RF-006: el precio no puede ser negativo. */
    public void setPrice(BigDecimal price) {
        if (price == null || price.signum() < 0) {
            throw new BusinessRuleException("RF-006", "El precio no puede ser negativo");
        }
        this.price = price;
    }

    public int getStock() { return stock; }
    public boolean isActive() { return active; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    @Override
    public String toString() {
        return code + " - " + name + " ($" + price + ", existencia " + stock + ")";
    }
}
