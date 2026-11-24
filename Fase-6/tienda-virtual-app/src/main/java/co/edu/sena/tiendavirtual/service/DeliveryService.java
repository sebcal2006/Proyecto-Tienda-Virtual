package co.edu.sena.tiendavirtual.service;

import co.edu.sena.tiendavirtual.entity.*;
import co.edu.sena.tiendavirtual.repository.DeliveryRepository;
import co.edu.sena.tiendavirtual.repository.OrderRepository;
import co.edu.sena.tiendavirtual.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/** Épica 8: asignar domiciliario (RF-035), sus pedidos (RF-036), en camino (RF-037) y entrega (RF-038). */
@Service
public class DeliveryService {

    private final DeliveryRepository deliveries;
    private final OrderRepository orders;
    private final UserRepository users;

    public DeliveryService(DeliveryRepository deliveries, OrderRepository orders, UserRepository users) {
        this.deliveries = deliveries;
        this.orders = orders;
        this.users = users;
    }

    public List<User> availableCouriers() {
        return users.findActiveByRole(Role.COURIER);
    }

    /** RF-035: el pedido en línea debe estar en preparación (ya pagado). */
    @Transactional
    public Delivery assign(Integer orderId, Integer courierId) {
        Order order = orders.findById(orderId).orElseThrow(() -> new BusinessException("Pedido no encontrado"));
        if (order.getChannel() != SalesChannel.ONLINE) {
            throw new BusinessException("Solo los pedidos en línea se entregan a domicilio");
        }
        if (order.getStatus() != OrderStatus.IN_PREPARATION) {
            throw new BusinessException("Solo se asigna domiciliario a pedidos en preparación");
        }
        if (availableCouriers().isEmpty()) {
            throw new BusinessException("No hay domiciliarios disponibles. El pedido queda sin asignar");
        }
        User courier = courierId == null ? null : users.findById(courierId)
                .filter(u -> u.isActive() && u.hasRole(Role.COURIER)).orElse(null);
        if (courier == null) {
            throw new BusinessException("Seleccione un domiciliario disponible");
        }
        Delivery delivery = deliveries.findByOrder(order).orElse(null);
        if (delivery == null) {
            return deliveries.save(new Delivery(order, courier));
        }
        if (delivery.getStatus() != DeliveryStatus.ASSIGNED) {
            throw new BusinessException("El domicilio ya salió; no se puede reasignar");
        }
        delivery.setCourier(courier);
        return delivery;
    }

    public List<Delivery> mine(User courier) {
        return deliveries.findByCourierOrderByAssignedAtDesc(courier);
    }

    /** RF-037. */
    @Transactional
    public Delivery markOnTheWay(Integer deliveryId, User courier) {
        Delivery delivery = own(deliveryId, courier);
        rejectIfFinished(delivery);
        if (delivery.getStatus() != DeliveryStatus.ASSIGNED) {
            throw new BusinessException("El pedido ya está en camino");
        }
        delivery.setStatus(DeliveryStatus.ON_THE_WAY);
        delivery.getOrder().changeStatus(OrderStatus.ON_THE_WAY, courier);
        return delivery;
    }

    /** RF-038 CA-01. */
    @Transactional
    public Delivery confirm(Integer deliveryId, User courier) {
        Delivery delivery = own(deliveryId, courier);
        rejectIfFinished(delivery);
        if (delivery.getStatus() != DeliveryStatus.ON_THE_WAY) {
            throw new BusinessException("Primero marque el pedido como en camino");
        }
        delivery.setStatus(DeliveryStatus.DELIVERED);
        delivery.setDeliveredAt(LocalDateTime.now());
        delivery.getOrder().changeStatus(OrderStatus.DELIVERED, courier);
        return delivery;
    }

    /** RF-038 CA-02: el cliente no recibe el pedido; se registra la incidencia y el pedido sigue en camino. */
    @Transactional
    public Delivery reportIncident(Integer deliveryId, User courier, String incident) {
        Delivery delivery = own(deliveryId, courier);
        rejectIfFinished(delivery);
        if (delivery.getStatus() != DeliveryStatus.ON_THE_WAY) {
            throw new BusinessException("Solo se registran incidencias de pedidos en camino");
        }
        if (incident == null || incident.isBlank()) {
            throw new BusinessException("Describa la incidencia");
        }
        delivery.setIncident(incident.trim());
        return delivery;
    }

    /** RF-036 CA-01: el domiciliario solo actúa sobre sus pedidos. */
    private Delivery own(Integer id, User courier) {
        return deliveries.findById(id)
                .filter(d -> d.getCourier().getId().equals(courier.getId()))
                .orElseThrow(() -> new BusinessException("Domicilio no encontrado"));
    }

    /** RF-037 CA-02. */
    private static void rejectIfFinished(Delivery delivery) {
        if (delivery.getStatus() == DeliveryStatus.DELIVERED) {
            throw new BusinessException("El pedido ya fue entregado");
        }
        if (delivery.getOrder().getStatus() == OrderStatus.CANCELLED) {
            throw new BusinessException("El pedido fue cancelado");
        }
    }
}
