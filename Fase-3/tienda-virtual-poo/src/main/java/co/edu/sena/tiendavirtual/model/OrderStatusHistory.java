package co.edu.sena.tiendavirtual.model;

import java.time.LocalDateTime;

/** Registro de cada cambio de estado del pedido (tabla order_status_history). RF-039. */
public class OrderStatusHistory {

    private Integer id;
    private final Order order;
    private final OrderStatus status;
    private final User changedBy;         // null si lo hizo el sistema o el cliente
    private final LocalDateTime changedAt = LocalDateTime.now();

    OrderStatusHistory(Order order, OrderStatus status, User changedBy) {
        this.order = order;
        this.status = status;
        this.changedBy = changedBy;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Order getOrder() { return order; }
    public OrderStatus getStatus() { return status; }
    public User getChangedBy() { return changedBy; }
    public LocalDateTime getChangedAt() { return changedAt; }

    @Override
    public String toString() {
        return status.getLabel() + (changedBy != null ? " (" + changedBy.getName() + ")" : "");
    }
}
