package co.edu.sena.tiendavirtual.web;

import co.edu.sena.tiendavirtual.entity.OrderStatus;
import co.edu.sena.tiendavirtual.entity.SalesChannel;
import co.edu.sena.tiendavirtual.service.BusinessException;
import co.edu.sena.tiendavirtual.service.CurrentUserService;
import co.edu.sena.tiendavirtual.service.DeliveryService;
import co.edu.sena.tiendavirtual.service.OrderService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * RF-028 (consultar pedidos), RF-029 (detalle), RF-030 (cancelar) y RF-031 (cambiar estado).
 * El administrador consulta; el vendedor además cambia el estado (RNF-002).
 */
@Controller
@RequestMapping("/gestion/pedidos")
public class OrderManagementController {

    private final OrderService orders;
    private final CurrentUserService current;
    private final DeliveryService deliveries;

    public OrderManagementController(OrderService orders, CurrentUserService current, DeliveryService deliveries) {
        this.orders = orders;
        this.current = current;
        this.deliveries = deliveries;
    }

    @GetMapping
    public String list(@RequestParam(required = false) OrderStatus estado,
                       @RequestParam(required = false) SalesChannel canal, Model model) {
        model.addAttribute("orders", orders.search(estado, canal));
        model.addAttribute("statuses", OrderStatus.values());
        model.addAttribute("channels", SalesChannel.values());
        model.addAttribute("estado", estado);
        model.addAttribute("canal", canal);
        return "gestion/pedidos";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Integer id, Model model) {
        model.addAttribute("order", orders.get(id));
        model.addAttribute("couriers", deliveries.availableCouriers());
        return "gestion/pedido-detalle";
    }

    @PostMapping("/{id}/avanzar")
    public String advance(@PathVariable Integer id, Authentication auth, RedirectAttributes flash) {
        try {
            var order = orders.advance(id, current.user(auth));
            flash.addFlashAttribute("ok", "Estado actualizado a " + order.getStatus().getLabel());
        } catch (BusinessException ex) {
            flash.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/gestion/pedidos/" + id;
    }

    /** RF-035. */
    @PostMapping("/{id}/asignar")
    public String assign(@PathVariable Integer id, @RequestParam(required = false) Integer courierId,
                         RedirectAttributes flash) {
        try {
            var delivery = deliveries.assign(id, courierId);
            flash.addFlashAttribute("ok", "Pedido asignado a " + delivery.getCourier().getName()
                    + ". Ya aparece en sus entregas");
        } catch (BusinessException ex) {
            flash.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/gestion/pedidos/" + id;
    }

    @PostMapping("/{id}/cancelar")
    public String cancel(@PathVariable Integer id, Authentication auth, RedirectAttributes flash) {
        try {
            orders.cancel(id, current.user(auth));
            flash.addFlashAttribute("ok", "Pedido cancelado. Las unidades volvieron al inventario");
        } catch (BusinessException ex) {
            flash.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/gestion/pedidos/" + id;
    }
}
