package co.edu.sena.tiendavirtual.model;

import co.edu.sena.tiendavirtual.exception.BusinessRuleException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Pedido de cualquiera de los dos canales (tabla orders).
 * Se crea con {@link Cart#checkout()} (en línea) o con {@link InStoreSale#confirm()} (presencial).
 * Aplica RN-003, RN-004, RN-005 y RN-008.
 */
public class Order {

    private Integer id;
    private final Customer customer;
    private final User seller;                     // solo canal presencial
    private DeliveryAddress deliveryAddress;
    private final SalesChannel channel;
    private OrderStatus status;
    private final List<OrderItem> items = new ArrayList<>();
    private BigDecimal total = BigDecimal.ZERO;
    private final LocalDateTime createdAt = LocalDateTime.now();

    private final List<OrderStatusHistory> statusHistory = new ArrayList<>();
    private final List<InventoryMovement> movements = new ArrayList<>();
    private final List<Payment> payments = new ArrayList<>();
    private Delivery delivery;
    private Invoice invoice;

    private Order(Customer customer, User seller, SalesChannel channel) {
        this.customer = Objects.requireNonNull(customer, "Cliente no encontrado");
        this.seller = seller;
        this.channel = channel;
    }

    /**
     * Crea el pedido, descuenta la existencia y registra los movimientos de salida (RN-005).
     * Antes valida que todos los productos tengan existencia (RN-002); si uno falla, no se
     * descuenta nada.
     */
    static Order create(Customer customer, User seller, SalesChannel channel,
                        Map<Product, Integer> lines, User registeredBy) {
        if (lines.isEmpty()) {
            throw new BusinessRuleException("RN-001", "El pedido debe tener al menos un producto");
        }
        lines.forEach((product, qty) -> product.checkAvailable(qty));

        Order order = new Order(customer, seller, channel);
        lines.forEach((product, qty) -> {
            order.items.add(new OrderItem(product, qty));
            order.movements.add(InventoryMovement.saleExit(product, qty, registeredBy, order));
        });
        order.total = order.calculateTotal();
        order.changeStatus(OrderStatus.PENDING, seller);
        return order;
    }

    /** RN-008: suma de cantidad x precio unitario de cada línea. */
    public BigDecimal calculateTotal() {
        return items.stream().map(OrderItem::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** RF-027 / RF-029: la dirección solo se cambia mientras el pedido está pendiente. */
    public void setDeliveryAddress(DeliveryAddress address) {
        if (status != OrderStatus.PENDING) {
            throw new BusinessRuleException("RF-029",
                    "El pedido ya está " + status.getLabel() + " y no se puede modificar");
        }
        this.deliveryAddress = Objects.requireNonNull(address);
    }

    /** RF-032: el cliente selecciona un método de pago y se crea un pago pendiente. */
    public Payment registerPayment(PaymentMethod method) {
        if (isPaid()) {
            throw new BusinessRuleException("RF-033", "El pedido ya tiene un pago aprobado");
        }
        if (status == OrderStatus.CANCELLED) {
            throw new BusinessRuleException("RF-033", "El pedido está cancelado");
        }
        Payment payment = new Payment(this, method);
        payments.add(payment);
        return payment;
    }

    public boolean isPaid() {
        return payments.stream().anyMatch(p -> p.getStatus() == PaymentStatus.APPROVED);
    }

    /**
     * RF-031 / RN-004: avanza al siguiente estado, sin saltos ni retrocesos.
     * Un pedido en línea solo pasa a preparación con el pago aprobado.
     */
    public void advanceStatus(User by) {
        OrderStatus next = status.next();
        if (next == null) {
            throw new BusinessRuleException("RN-004",
                    "El pedido ya está " + status.getLabel() + " y no puede avanzar");
        }
        if (next == OrderStatus.IN_PREPARATION && channel == SalesChannel.ONLINE && !isPaid()) {
            throw new BusinessRuleException("RN-004", "El pedido no tiene un pago aprobado");
        }
        changeStatus(next, by);
    }

    /** RF-030 / RN-003 / RN-005: cancela y devuelve las unidades al inventario. */
    public void cancel(User by) {
        if (!status.canBeCancelled()) {
            throw new BusinessRuleException("RN-003",
                    "No se puede cancelar un pedido " + status.getLabel());
        }
        for (OrderItem item : items) {
            movements.add(InventoryMovement.cancellationEntry(item.getProduct(), item.getQuantity(), by, this));
        }
        changeStatus(OrderStatus.CANCELLED, by);
    }

    private void changeStatus(OrderStatus newStatus, User by) {
        this.status = newStatus;
        statusHistory.add(new OrderStatusHistory(this, newStatus, by));
    }

    void attachDelivery(Delivery delivery) {
        if (this.delivery != null) {
            throw new BusinessRuleException("RF-035", "El pedido ya tiene un domiciliario asignado");
        }
        this.delivery = delivery;
    }

    void attachInvoice(Invoice invoice) {
        this.invoice = invoice;
    }

    /** Resumen de cantidades por producto, útil para mostrar el pedido. */
    public Map<String, Integer> quantitiesByCode() {
        Map<String, Integer> map = new LinkedHashMap<>();
        items.forEach(i -> map.put(i.getProduct().getCode(), i.getQuantity()));
        return map;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Customer getCustomer() { return customer; }
    public User getSeller() { return seller; }
    public DeliveryAddress getDeliveryAddress() { return deliveryAddress; }
    public SalesChannel getChannel() { return channel; }
    public OrderStatus getStatus() { return status; }
    public List<OrderItem> getItems() { return Collections.unmodifiableList(items); }
    public BigDecimal getTotal() { return total; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public List<OrderStatusHistory> getStatusHistory() { return Collections.unmodifiableList(statusHistory); }
    public List<InventoryMovement> getMovements() { return Collections.unmodifiableList(movements); }
    public List<Payment> getPayments() { return Collections.unmodifiableList(payments); }
    public Delivery getDelivery() { return delivery; }
    public Invoice getInvoice() { return invoice; }

    @Override
    public String toString() {
        return "Pedido " + channel + " de " + customer + " - " + status.getLabel() + " - $" + total;
    }
}
