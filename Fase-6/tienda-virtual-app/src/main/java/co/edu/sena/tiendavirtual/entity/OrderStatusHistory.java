package co.edu.sena.tiendavirtual.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/** Tabla order_status_history (RF-039). */
@Entity
@Table(name = "order_status_history")
public class OrderStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "order_id")
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @ManyToOne
    @JoinColumn(name = "changed_by")
    private User changedBy;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    protected OrderStatusHistory() {
    }

    public OrderStatusHistory(Order order, OrderStatus status, User changedBy) {
        this.order = order;
        this.status = status;
        this.changedBy = changedBy;
        this.changedAt = LocalDateTime.now();
    }

    public Integer getId() { return id; }
    public Order getOrder() { return order; }
    public OrderStatus getStatus() { return status; }
    public User getChangedBy() { return changedBy; }
    public LocalDateTime getChangedAt() { return changedAt; }
}
