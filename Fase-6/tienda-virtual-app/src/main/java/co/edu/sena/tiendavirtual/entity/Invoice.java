package co.edu.sena.tiendavirtual.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Tabla invoices: una factura por pedido, con número único (RF-041). */
@Entity
@Table(name = "invoices")
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne(optional = false)
    @JoinColumn(name = "order_id", unique = true)
    private Order order;

    @ManyToOne(optional = false)
    @JoinColumn(name = "issued_by")
    private User issuedBy;

    @Column(name = "invoice_number", nullable = false, unique = true, length = 20)
    private String number;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal subtotal;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal tax;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal total;

    @Column(name = "issued_at", nullable = false)
    private LocalDateTime issuedAt;

    protected Invoice() {
    }

    public Invoice(Order order, User issuedBy, String number, BigDecimal subtotal, BigDecimal tax) {
        this.order = order;
        this.issuedBy = issuedBy;
        this.number = number;
        this.subtotal = subtotal;
        this.tax = tax;
        this.total = subtotal.add(tax);
        this.issuedAt = LocalDateTime.now();
    }

    public Integer getId() { return id; }
    public Order getOrder() { return order; }
    public User getIssuedBy() { return issuedBy; }
    public String getNumber() { return number; }
    public BigDecimal getSubtotal() { return subtotal; }
    public BigDecimal getTax() { return tax; }
    public BigDecimal getTotal() { return total; }
    public LocalDateTime getIssuedAt() { return issuedAt; }
}
