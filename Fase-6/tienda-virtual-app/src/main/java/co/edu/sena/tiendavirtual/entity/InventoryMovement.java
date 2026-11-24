package co.edu.sena.tiendavirtual.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/** Tabla inventory_movements: cada entrada o salida de unidades (RN-005, RF-014). */
@Entity
@Table(name = "inventory_movements")
public class InventoryMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MovementType type;

    @Column(nullable = false)
    private int quantity;

    @Column(length = 200)
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected InventoryMovement() {
    }

    public InventoryMovement(Product product, User user, Order order, MovementType type, int quantity, String notes) {
        this.product = product;
        this.user = user;
        this.order = order;
        this.type = type;
        this.quantity = quantity;
        this.notes = notes;
    }

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    public Integer getId() { return id; }
    public Product getProduct() { return product; }
    public User getUser() { return user; }
    public Order getOrder() { return order; }
    public MovementType getType() { return type; }
    public int getQuantity() { return quantity; }
    public String getNotes() { return notes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
