package co.edu.sena.tiendavirtual.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/** Tabla deliveries: un pedido en línea tiene como máximo un domicilio (RF-035 a RF-038). */
@Entity
@Table(name = "deliveries")
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne(optional = false)
    @JoinColumn(name = "order_id", unique = true)
    private Order order;

    @ManyToOne(optional = false)
    @JoinColumn(name = "courier_id")
    private User courier;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeliveryStatus status = DeliveryStatus.ASSIGNED;

    @Column(length = 300)
    private String incident;

    @Column(name = "assigned_at", nullable = false)
    private LocalDateTime assignedAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    protected Delivery() {
    }

    public Delivery(Order order, User courier) {
        this.order = order;
        this.courier = courier;
        this.assignedAt = LocalDateTime.now();
    }

    public Integer getId() { return id; }
    public Order getOrder() { return order; }
    public User getCourier() { return courier; }
    public void setCourier(User courier) { this.courier = courier; this.assignedAt = LocalDateTime.now(); }
    public DeliveryStatus getStatus() { return status; }
    public void setStatus(DeliveryStatus status) { this.status = status; }
    public String getIncident() { return incident; }
    public void setIncident(String incident) { this.incident = incident; }
    public LocalDateTime getAssignedAt() { return assignedAt; }
    public LocalDateTime getDeliveredAt() { return deliveredAt; }
    public void setDeliveredAt(LocalDateTime deliveredAt) { this.deliveredAt = deliveredAt; }
}
