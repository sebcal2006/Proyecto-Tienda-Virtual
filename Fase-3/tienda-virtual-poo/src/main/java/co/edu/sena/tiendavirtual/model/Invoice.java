package co.edu.sena.tiendavirtual.model;

import co.edu.sena.tiendavirtual.exception.BusinessRuleException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * Factura de un pedido (tabla invoices). RF-041, RF-042, RN-006.
 * total = subtotal + impuesto (mismo CHECK de la base de datos).
 */
public class Invoice {

    /** IVA general en Colombia. */
    public static final BigDecimal DEFAULT_TAX_RATE = new BigDecimal("0.19");

    private Integer id;
    private final Order order;
    private final User issuedBy;
    private final String invoiceNumber;
    private final BigDecimal subtotal;
    private final BigDecimal tax;
    private final BigDecimal total;
    private final LocalDateTime issuedAt = LocalDateTime.now();

    private Invoice(Order order, User issuedBy, String invoiceNumber, BigDecimal taxRate) {
        this.order = order;
        this.issuedBy = issuedBy;
        this.invoiceNumber = invoiceNumber;
        this.subtotal = order.getTotal();
        this.tax = subtotal.multiply(taxRate).setScale(2, RoundingMode.HALF_UP);
        this.total = subtotal.add(tax);
    }

    /**
     * RF-041 / RN-006: solo con pago aprobado (en línea) o venta finalizada (presencial),
     * una sola factura por pedido.
     */
    public static Invoice generate(Order order, User issuedBy, String invoiceNumber, BigDecimal taxRate) {
        issuedBy.requireRole(RoleName.BILLING_MANAGER);
        if (invoiceNumber == null || invoiceNumber.isBlank()) {
            throw new BusinessRuleException("RF-041", "El número de factura es obligatorio");
        }
        if (order.getInvoice() != null) {
            throw new BusinessRuleException("RF-041", "El pedido ya tiene una factura generada");
        }
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new BusinessRuleException("RN-006", "No se factura un pedido cancelado");
        }
        if (order.getChannel() == SalesChannel.ONLINE && !order.isPaid()) {
            throw new BusinessRuleException("RN-006", "El pedido no tiene un pago aprobado");
        }
        Invoice invoice = new Invoice(order, issuedBy, invoiceNumber.trim(), taxRate);
        order.attachInvoice(invoice);
        return invoice;
    }

    /** RF-042 / RNF-002: la consultan el encargado de facturación y el administrador. */
    public static boolean canBeViewedBy(User user) {
        return user.canView(RoleName.BILLING_MANAGER);
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Order getOrder() { return order; }
    public User getIssuedBy() { return issuedBy; }
    public String getInvoiceNumber() { return invoiceNumber; }
    public BigDecimal getSubtotal() { return subtotal; }
    public BigDecimal getTax() { return tax; }
    public BigDecimal getTotal() { return total; }
    public LocalDateTime getIssuedAt() { return issuedAt; }

    @Override
    public String toString() {
        return invoiceNumber + ": subtotal $" + subtotal + " + IVA $" + tax + " = $" + total;
    }
}
