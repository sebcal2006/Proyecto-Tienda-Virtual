package co.edu.sena.tiendavirtual.web;

import co.edu.sena.tiendavirtual.service.BusinessException;
import co.edu.sena.tiendavirtual.service.CurrentUserService;
import co.edu.sena.tiendavirtual.service.DeliveryService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Módulo del domiciliario: sus pedidos (RF-036), en camino (RF-037) y entrega o incidencia (RF-038). */
@Controller
@RequestMapping("/domiciliario")
public class CourierController {

    private final DeliveryService deliveries;
    private final CurrentUserService current;

    public CourierController(DeliveryService deliveries, CurrentUserService current) {
        this.deliveries = deliveries;
        this.current = current;
    }

    @GetMapping
    public String list(Authentication auth, Model model) {
        var mine = deliveries.mine(current.user(auth));
        model.addAttribute("active", mine.stream()
                .filter(d -> d.getStatus() != co.edu.sena.tiendavirtual.entity.DeliveryStatus.DELIVERED).toList());
        model.addAttribute("done", mine.stream()
                .filter(d -> d.getStatus() == co.edu.sena.tiendavirtual.entity.DeliveryStatus.DELIVERED).toList());
        return "domiciliario/index";
    }

    @PostMapping("/{id}/en-camino")
    public String onTheWay(@PathVariable Integer id, Authentication auth, RedirectAttributes flash) {
        try {
            var d = deliveries.markOnTheWay(id, current.user(auth));
            flash.addFlashAttribute("ok", "Pedido " + d.getOrder().getCode() + " en camino");
        } catch (BusinessException ex) {
            flash.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/domiciliario";
    }

    @PostMapping("/{id}/entregado")
    public String delivered(@PathVariable Integer id, Authentication auth, RedirectAttributes flash) {
        try {
            var d = deliveries.confirm(id, current.user(auth));
            flash.addFlashAttribute("ok", "Entrega del pedido " + d.getOrder().getCode() + " confirmada");
        } catch (BusinessException ex) {
            flash.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/domiciliario";
    }

    @PostMapping("/{id}/incidencia")
    public String incident(@PathVariable Integer id, @RequestParam(required = false) String incident,
                           Authentication auth, RedirectAttributes flash) {
        try {
            var d = deliveries.reportIncident(id, current.user(auth), incident);
            flash.addFlashAttribute("ok", "Incidencia registrada para el pedido " + d.getOrder().getCode());
        } catch (BusinessException ex) {
            flash.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/domiciliario";
    }
}
