package co.edu.sena.tiendavirtual.service;

import co.edu.sena.tiendavirtual.entity.Invoice;
import co.edu.sena.tiendavirtual.entity.Order;
import co.edu.sena.tiendavirtual.entity.User;
import co.edu.sena.tiendavirtual.repository.InvoiceRepository;
import co.edu.sena.tiendavirtual.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/** Épica 10: generar factura (RF-041) y consultar facturas (RF-042). IVA del 19 % sobre el total del pedido. */
@Service
public class InvoiceService {

    static final BigDecimal VAT = new BigDecimal("0.19");

    private final InvoiceRepository invoices;
    private final OrderRepository orders;

    public InvoiceService(InvoiceRepository invoices, OrderRepository orders) {
        this.invoices = invoices;
        this.orders = orders;
    }

    /** Pedidos listos para facturar: no cancelados, sin factura y, si son en línea, pagados. */
    public List<Order> pending() {
        return orders.findWithoutInvoice().stream().filter(Order::isBillable).toList();
    }

    public List<Invoice> all() {
        return invoices.findAllByOrderByIdDesc();
    }

    public Invoice get(Integer id) {
        return invoices.findById(id).orElseThrow(() -> new BusinessException("Factura no encontrada"));
    }

    @Transactional
    public Invoice generate(Integer orderId, User issuedBy) {
        Order order = orders.findById(orderId).orElseThrow(() -> new BusinessException("Pedido no encontrado"));
        invoices.findByOrder(order).ifPresent(existing -> {
            throw new BusinessException("El pedido " + order.getCode() + " ya tiene la factura "
                    + existing.getNumber() + "; no se crea una duplicada");
        });
        if (!order.isBillable()) {
            throw new BusinessException("El pedido " + order.getCode()
                    + " no se puede facturar: está cancelado o no tiene un pago aprobado");
        }
        BigDecimal subtotal = order.getTotal();
        BigDecimal tax = subtotal.multiply(VAT).setScale(2, RoundingMode.HALF_UP);
        return invoices.save(new Invoice(order, issuedBy, nextNumber(), subtotal, tax));
    }

    /** Número consecutivo único: FV-0001, FV-0002... */
    String nextNumber() {
        int max = invoices.findAll().stream()
                .map(Invoice::getNumber)
                .filter(n -> n != null && n.matches("FV-\\d+"))
                .mapToInt(n -> Integer.parseInt(n.substring(3)))
                .max().orElse(0);
        return String.format("FV-%04d", max + 1);
    }
}
