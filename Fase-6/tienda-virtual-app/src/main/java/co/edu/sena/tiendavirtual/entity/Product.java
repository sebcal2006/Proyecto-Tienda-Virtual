package co.edu.sena.tiendavirtual.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Tabla products. La existencia (stock) solo cambia con movimientos de inventario. */
@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private int stock;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected Product() {
    }

    /** Usado por las pruebas unitarias; en la aplicación los productos vienen de la base de datos. */
    public Product(String code, String name, BigDecimal price, int stock) {
        this.code = code;
        this.name = name;
        this.price = price;
        this.stock = stock;
    }

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    /** RN-002. */
    public boolean isAvailable() {
        return active && stock > 0;
    }

    /** Menos de 10 unidades: se muestra "Últimas N unidades". */
    public boolean isLowStock() {
        return stock > 0 && stock <= 10;
    }

    public Integer getId() { return id; }
    public Category getCategory() { return category; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public BigDecimal getPrice() { return price; }
    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }
    public boolean isActive() { return active; }
}
