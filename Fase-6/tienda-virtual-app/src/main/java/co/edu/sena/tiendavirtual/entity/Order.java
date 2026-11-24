package co.edu.sena.tiendavirtual.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Tabla orders. El nombre de entidad es SalesOrder porque ORDER es palabra
 * reservada en JPQL.
 */
@Entity(name = "SalesOrder")
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "seller_id")
    private User seller;

    @ManyToOne
    @JoinColumn(name = "delivery_address_id")
    private DeliveryAddress deliveryAddress;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SalesChannel channel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.PENDING;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<OrderItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    @OrderBy("changedAt ASC, id ASC")
    private List<OrderStatusHistory> history = new ArrayList<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    @OrderBy("id ASC")
    private List<Payment> payments = new ArrayList<>();

    protected Order() {
    }

    public Order(Customer customer, User seller, SalesChannel channel) {
        this.customer = customer;
        this.seller = seller;
        this.channel = channel;
    }

    /** Domicilio asignado (solo pedidos en línea). */
    @OneToOne(mappedBy = "order")
    private Delivery delivery;

    /** Factura generada (RF-041). */
    @OneToOne(mappedBy = "order")
    private Invoice invoice;

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    /** RN-008: suma de cantidad x precio unitario. */
    public BigDecimal calculateTotal() {
        return items.stream().map(OrderItem::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public boolean isPaid() {
        return payments.stream().anyMatch(p -> p.getStatus() == PaymentStatus.APPROVED);
    }

    /** Pedido en línea pendiente sin pago aprobado: el cliente puede reintentar el pago. */
    public boolean isAwaitingPayment() {
        return channel == SalesChannel.ONLINE && status == OrderStatus.PENDING && !isPaid();
    }

    public String getCode() {
        return id == null ? "" : String.format("PED-%04d", id);
    }

    public int getUnits() {
        return items.stream().mapToInt(OrderItem::getQuantity).sum();
    }

    public void changeStatus(OrderStatus newStatus, User by) {
        this.status = newStatus;
        history.add(new OrderStatusHistory(this, newStatus, by));
    }

    public Delivery getDelivery() { return delivery; }
    public Invoice getInvoice() { return invoice; }

    /** Pedido que se puede facturar: no cancelado y, si es en línea, con pago aprobado. */
    public boolean isBillable() {
        return status != OrderStatus.CANCELLED && (channel == SalesChannel.IN_STORE || isPaid());
    }

    public Integer getId() { return id; }
    public Customer getCustomer() { return customer; }
    public User getSeller() { return seller; }
    public DeliveryAddress getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(DeliveryAddress deliveryAddress) { this.deliveryAddress = deliveryAddress; }
    public SalesChannel getChannel() { return channel; }
    public OrderStatus getStatus() { return status; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public List<OrderItem> getItems() { return items; }
    public List<OrderStatusHistory> getHistory() { return history; }
    public List<Payment> getPayments() { return payments; }
}
