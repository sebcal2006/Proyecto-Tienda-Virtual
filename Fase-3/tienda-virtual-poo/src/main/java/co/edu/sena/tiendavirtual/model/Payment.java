package co.edu.sena.tiendavirtual.model;

import co.edu.sena.tiendavirtual.exception.BusinessRuleException;

import java.time.LocalDateTime;

/**
 * Pago de un pedido (tabla payments). RF-032 a RF-034.
 * RNF-004: no se guarda ningún dato de la tarjeta, solo el método y el estado.
 */
public class Payment {

    private Integer id;
    private final Order order;
    private final PaymentMethod method;
    private PaymentStatus status = PaymentStatus.PENDING;
    private final LocalDateTime createdAt = LocalDateTime.now();

    Payment(Order order, PaymentMethod method) {
        if (method == null || !method.isActive()) {
            throw new BusinessRuleException("RF-032", "El método de pago no está disponible");
        }
        this.order = order;
        this.method = method;
    }

    /** Resultado aprobado de la pasarela de pago. */
    public void approve() {
        checkPending();
        status = PaymentStatus.APPROVED;
    }

    /** Resultado rechazado: el cliente puede intentar con otro método. */
    public void reject() {
        checkPending();
        status = PaymentStatus.REJECTED;
    }

    private void checkPending() {
        if (status != PaymentStatus.PENDING) {
            throw new BusinessRuleException("RF-033", "El pago ya fue procesado");
        }
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Order getOrder() { return order; }
    public PaymentMethod getMethod() { return method; }
    public PaymentStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    @Override
    public String toString() {
        return method + ": " + status.getLabel();
    }
}
