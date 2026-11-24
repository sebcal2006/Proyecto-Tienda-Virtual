package co.edu.sena.tiendavirtual.model;

import co.edu.sena.tiendavirtual.exception.BusinessRuleException;

import java.time.LocalDateTime;

/**
 * Domicilio de un pedido (tabla deliveries). RF-035 a RF-038.
 * Al avanzar la entrega también avanza el estado del pedido (RN-004).
 */
public class Delivery {

    private Integer id;
    private final Order order;
    private final User courier;
    private DeliveryStatus status = DeliveryStatus.ASSIGNED;
    private String incident;
    private final LocalDateTime assignedAt = LocalDateTime.now();
    private LocalDateTime deliveredAt;

    private Delivery(Order order, User courier) {
        this.order = order;
        this.courier = courier;
    }

    /** RF-035: el vendedor asigna un pedido en preparación a un domiciliario. */
    public static Delivery assign(Order order, User courier, User seller) {
        seller.requireRole(RoleName.SELLER);
        courier.requireRole(RoleName.COURIER);
        if (order.getStatus() != OrderStatus.IN_PREPARATION) {
            throw new BusinessRuleException("RF-035", "Solo se asignan pedidos en preparación");
        }
        if (order.getDeliveryAddress() == null) {
            throw new BusinessRuleException("RF-035", "El pedido no tiene dirección de entrega");
        }
        Delivery delivery = new Delivery(order, courier);
        order.attachDelivery(delivery);
        return delivery;
    }

    /** RF-037 (HU37): el domiciliario sale a entregar y el pedido pasa a "en camino". */
    public void markOnTheWay(User by) {
        checkCourier(by);
        if (status != DeliveryStatus.ASSIGNED) {
            throw new BusinessRuleException("RN-004", "La entrega ya salió");
        }
        status = DeliveryStatus.ON_THE_WAY;
        order.advanceStatus(by);
    }

    /** RF-038: el domiciliario confirma que el cliente recibió el pedido. */
    public void confirmDelivered(User by) {
        checkCourier(by);
        if (status != DeliveryStatus.ON_THE_WAY) {
            throw new BusinessRuleException("RN-004", "La entrega debe estar en camino para confirmarla");
        }
        status = DeliveryStatus.DELIVERED;
        deliveredAt = LocalDateTime.now();
        order.advanceStatus(by);
    }

    /** RF-038 E1: el cliente no recibe el pedido. */
    public void reportIncident(User by, String description) {
        checkCourier(by);
        if (description == null || description.isBlank()) {
            throw new BusinessRuleException("RF-038", "Describa la novedad de la entrega");
        }
        this.incident = description.trim();
    }

    private void checkCourier(User by) {
        if (by != courier) {
            throw new BusinessRuleException("RNF-002", "Este pedido no está asignado a este domiciliario");
        }
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Order getOrder() { return order; }
    public User getCourier() { return courier; }
    public DeliveryStatus getStatus() { return status; }
    public String getIncident() { return incident; }
    public LocalDateTime getAssignedAt() { return assignedAt; }
    public LocalDateTime getDeliveredAt() { return deliveredAt; }
}
