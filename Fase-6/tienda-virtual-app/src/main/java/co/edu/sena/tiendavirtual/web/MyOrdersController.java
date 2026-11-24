package co.edu.sena.tiendavirtual.web;

import co.edu.sena.tiendavirtual.entity.Customer;
import co.edu.sena.tiendavirtual.entity.OrderStatus;
import co.edu.sena.tiendavirtual.repository.PaymentMethodRepository;
import co.edu.sena.tiendavirtual.service.CurrentUserService;
import co.edu.sena.tiendavirtual.service.OrderService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/** RF-039 (seguimiento del pedido), RF-040 (historial de compras) y RF-034 (estado del pago). */
@Controller
@RequestMapping("/mis-pedidos")
public class MyOrdersController {

    private final OrderService orders;
    private final CurrentUserService current;
    private final PaymentMethodRepository methods;

    public MyOrdersController(OrderService orders, CurrentUserService current, PaymentMethodRepository methods) {
        this.orders = orders;
        this.current = current;
        this.methods = methods;
    }

    @GetMapping
    public String list(Authentication auth, Model model) {
        model.addAttribute("orders", orders.history(current.customer(auth)));
        return "mis-pedidos";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Integer id, Authentication auth, Model model) {
        Customer customer = current.customer(auth);
        model.addAttribute("order", orders.ownOrder(id, customer));
        model.addAttribute("steps", new OrderStatus[] {
                OrderStatus.PENDING, OrderStatus.IN_PREPARATION, OrderStatus.ON_THE_WAY, OrderStatus.DELIVERED });
        model.addAttribute("methods", methods.findByActiveTrueOrderByNameAsc());
        return "mis-pedido-detalle";
    }
}
