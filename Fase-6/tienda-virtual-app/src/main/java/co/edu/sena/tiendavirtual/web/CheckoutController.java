package co.edu.sena.tiendavirtual.web;

import co.edu.sena.tiendavirtual.entity.Cart;
import co.edu.sena.tiendavirtual.entity.Order;
import co.edu.sena.tiendavirtual.repository.PaymentMethodRepository;
import co.edu.sena.tiendavirtual.service.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * RF-026 (confirmar compra), RF-027 (dirección de entrega), RF-032 (pago en línea)
 * y RF-033 (reintentar pago rechazado). La pasarela de pagos es simulada: el cliente
 * puede marcar "simular rechazo" para probar el flujo de pago rechazado.
 */
@Controller
public class CheckoutController {

    private final CartService carts;
    private final OrderService orders;
    private final CurrentUserService current;
    private final PaymentMethodRepository methods;

    public CheckoutController(CartService carts, OrderService orders, CurrentUserService current,
                              PaymentMethodRepository methods) {
        this.carts = carts;
        this.orders = orders;
        this.current = current;
        this.methods = methods;
    }

    @GetMapping("/checkout")
    public String form(Authentication auth, Model model, RedirectAttributes flash) {
        Cart cart = carts.cartOf(current.customer(auth));
        if (cart.getItems().isEmpty()) {
            flash.addFlashAttribute("error", "El carrito está vacío");
            return "redirect:/carrito";
        }
        model.addAttribute("cart", cart);
        model.addAttribute("methods", methods.findByActiveTrueOrderByNameAsc());
        return "checkout";
    }

    @PostMapping("/checkout")
    public String confirm(@RequestParam String address, @RequestParam String city,
                          @RequestParam(required = false) String reference,
                          @RequestParam(required = false) Integer paymentMethodId,
                          @RequestParam(defaultValue = "false") boolean simulateRejection,
                          Authentication auth, RedirectAttributes flash) {
        try {
            Order order = orders.checkout(current.customer(auth), current.user(auth),
                    new OrderService.CheckoutData(address, city, reference, paymentMethodId, simulateRejection));
            if (order.isPaid()) {
                flash.addFlashAttribute("ok", "Compra confirmada. Pago aprobado para el pedido " + order.getCode());
            } else {
                flash.addFlashAttribute("error", "El pago fue rechazado. Puede reintentar con otro método de pago");
            }
            return "redirect:/mis-pedidos/" + order.getId();
        } catch (BusinessException ex) {
            flash.addFlashAttribute("error", ex.getMessage());
            flash.addFlashAttribute("address", address);
            flash.addFlashAttribute("city", city);
            flash.addFlashAttribute("reference", reference);
            return "redirect:/checkout";
        }
    }

    @PostMapping("/mis-pedidos/{id}/pagar")
    public String retry(@PathVariable Integer id, @RequestParam(required = false) Integer paymentMethodId,
                        @RequestParam(defaultValue = "false") boolean simulateRejection,
                        Authentication auth, RedirectAttributes flash) {
        try {
            Order order = orders.retryPayment(id, current.customer(auth), paymentMethodId, simulateRejection);
            if (order.isPaid()) {
                flash.addFlashAttribute("ok", "Pago aprobado");
            } else {
                flash.addFlashAttribute("error", "El pago fue rechazado nuevamente");
            }
        } catch (BusinessException ex) {
            flash.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/mis-pedidos/" + id;
    }
}
