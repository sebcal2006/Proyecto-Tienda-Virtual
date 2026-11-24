package co.edu.sena.tiendavirtual.service;

import co.edu.sena.tiendavirtual.entity.*;
import co.edu.sena.tiendavirtual.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Pedidos de los dos canales. Reglas: RN-001 (carrito no vacío), RN-002 (existencia),
 * RN-003 (cancelación), RN-004 (orden de estados), RN-005 (descontar y devolver
 * inventario), RN-008 (total) y RNF-008 (bloqueo de fila para no vender dos veces).
 */
@Service
public class OrderService {

    private final OrderRepository orders;
    private final ProductRepository products;
    private final InventoryMovementRepository movements;
    private final CartRepository carts;
    private final DeliveryAddressRepository addresses;
    private final PaymentMethodRepository paymentMethods;
    private final CustomerService customers;
    private final DeliveryRepository deliveries;

    public OrderService(OrderRepository orders, ProductRepository products, InventoryMovementRepository movements,
                        CartRepository carts, DeliveryAddressRepository addresses,
                        PaymentMethodRepository paymentMethods, CustomerService customers,
                        DeliveryRepository deliveries) {
        this.orders = orders;
        this.products = products;
        this.movements = movements;
        this.carts = carts;
        this.addresses = addresses;
        this.paymentMethods = paymentMethods;
        this.customers = customers;
        this.deliveries = deliveries;
    }

    /** Datos del formulario de compra en línea (RF-026, RF-027, RF-032). */
    public record CheckoutData(String address, String city, String reference, Integer paymentMethodId,
                               boolean simulateRejection) {
    }

    // ------------------------------------------------------------------ canal en línea

    /** RF-026: confirma la compra del carrito, registra la dirección y procesa el pago. */
    @Transactional
    public Order checkout(Customer customer, User user, CheckoutData data) {
        Cart cart = carts.findByCustomer(customer).orElse(null);
        if (cart == null || cart.getItems().isEmpty()) {
            throw new BusinessException("El carrito está vacío");
        }
        if (isBlank(data.address()) || isBlank(data.city())) {
            throw new BusinessException("La dirección y la ciudad son obligatorias");
        }
        PaymentMethod method = method(data.paymentMethodId());

        Map<Integer, Integer> lines = new LinkedHashMap<>();
        cart.getItems().forEach(i -> lines.put(i.getProduct().getId(), i.getQuantity()));

        Order order = createOrder(customer, null, SalesChannel.ONLINE, lines, user);
        order.setDeliveryAddress(addresses.save(new DeliveryAddress(
                data.address().trim(), data.city().trim(), isBlank(data.reference()) ? null : data.reference().trim())));
        order.getPayments().add(new Payment(order, method,
                data.simulateRejection() ? PaymentStatus.REJECTED : PaymentStatus.APPROVED));
        cart.getItems().clear();
        return order;
    }

    /** RF-033: el cliente reintenta el pago de un pedido rechazado. */
    @Transactional
    public Order retryPayment(Integer orderId, Customer customer, Integer paymentMethodId, boolean simulateRejection) {
        Order order = ownOrder(orderId, customer);
        if (!order.isAwaitingPayment()) {
            throw new BusinessException("Este pedido no tiene pagos pendientes");
        }
        order.getPayments().add(new Payment(order, method(paymentMethodId),
                simulateRejection ? PaymentStatus.REJECTED : PaymentStatus.APPROVED));
        return order;
    }

    // ------------------------------------------------------------------ canal presencial

    /** RF-019 / RF-022: confirma la venta presencial o telefónica del vendedor. */
    @Transactional
    public Order confirmInStoreSale(SaleDraft draft, User seller) {
        if (draft.getCustomerId() == null) {
            throw new BusinessException("Seleccione o registre el cliente de la venta");
        }
        if (draft.isEmpty()) {
            throw new BusinessException("Agregue al menos un producto a la venta");
        }
        Customer customer = customers.get(draft.getCustomerId());
        return createOrder(customer, seller, SalesChannel.IN_STORE, draft.getLines(), seller);
    }

    // ------------------------------------------------------------------ gestión del pedido

    public List<Order> search(OrderStatus status, SalesChannel channel) {
        return orders.search(status, channel);
    }

    public List<Order> history(Customer customer) {
        return orders.findByCustomerOrderByCreatedAtDesc(customer);
    }

    public Order get(Integer id) {
        return orders.findById(id).orElseThrow(() -> new BusinessException("Pedido no encontrado"));
    }

    /** El cliente solo ve sus propios pedidos (RNF-002). */
    public Order ownOrder(Integer id, Customer customer) {
        Order order = get(id);
        if (!order.getCustomer().getId().equals(customer.getId())) {
            throw new BusinessException("Pedido no encontrado");
        }
        return order;
    }

    /** RF-031 / RN-004: avanza al siguiente estado. En línea, solo con pago aprobado. */
    @Transactional
    public Order advance(Integer orderId, User by) {
        Order order = get(orderId);
        OrderStatus next = order.getStatus().next();
        if (next == null) {
            throw new BusinessException("El pedido ya está " + order.getStatus().getLabel().toLowerCase()
                    + " y no puede avanzar");
        }
        if (next == OrderStatus.IN_PREPARATION && order.getChannel() == SalesChannel.ONLINE && !order.isPaid()) {
            throw new BusinessException("El pedido no tiene un pago aprobado");
        }
        if (order.getChannel() == SalesChannel.ONLINE && next != OrderStatus.IN_PREPARATION) {
            throw new BusinessException("Los pedidos en línea los lleva el domiciliario: asígnelo para continuar");
        }
        order.changeStatus(next, by);
        return order;
    }

    /** RF-030 / RN-003 / RN-005: cancela y devuelve las unidades al inventario. */
    @Transactional
    public Order cancel(Integer orderId, User by) {
        Order order = get(orderId);
        if (!order.getStatus().canBeCancelled()) {
            throw new BusinessException("No se puede cancelar un pedido " + order.getStatus().getLabel().toLowerCase());
        }
        for (OrderItem item : order.getItems()) {
            Product product = lock(item.getProduct().getId());
            product.setStock(product.getStock() + item.getQuantity());
            movements.save(new InventoryMovement(product, by, order, MovementType.ENTRY, item.getQuantity(),
                    "Devolución por cancelación del pedido " + order.getCode()));
        }
        if (order.getDelivery() != null) {
            deliveries.delete(order.getDelivery());
        }
        order.changeStatus(OrderStatus.CANCELLED, by);
        return order;
    }

    // ------------------------------------------------------------------ núcleo común

    /**
     * Crea el pedido: valida todas las existencias con la fila bloqueada (RN-002, RNF-008),
     * calcula el total (RN-008), descuenta el inventario y registra las salidas (RN-005).
     */
    private Order createOrder(Customer customer, User seller, SalesChannel channel,
                              Map<Integer, Integer> lines, User registeredBy) {
        Map<Product, Integer> locked = new LinkedHashMap<>();
        for (Map.Entry<Integer, Integer> line : lines.entrySet()) {
            Product product = lock(line.getKey());
            CartService.checkAvailable(product, line.getValue());
            locked.put(product, line.getValue());
        }
        Order order = new Order(customer, seller, channel);
        locked.forEach((product, qty) -> order.getItems().add(new OrderItem(order, product, qty)));
        order.setTotal(order.calculateTotal());
        order.changeStatus(OrderStatus.PENDING, registeredBy);
        orders.save(order);

        locked.forEach((product, qty) -> {
            product.setStock(product.getStock() - qty);
            movements.save(new InventoryMovement(product, registeredBy, order, MovementType.EXIT, qty,
                    "Venta " + order.getCode()));
        });
        return order;
    }

    private Product lock(Integer productId) {
        return products.findByIdForUpdate(productId)
                .orElseThrow(() -> new BusinessException("Producto no encontrado"));
    }

    private PaymentMethod method(Integer id) {
        PaymentMethod method = id == null ? null : paymentMethods.findById(id).orElse(null);
        if (method == null || !method.isActive()) {
            throw new BusinessException("Seleccione un método de pago disponible");
        }
        return method;
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
