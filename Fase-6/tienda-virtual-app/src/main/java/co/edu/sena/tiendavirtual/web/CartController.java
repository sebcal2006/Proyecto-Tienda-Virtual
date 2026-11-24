package co.edu.sena.tiendavirtual.web;

import co.edu.sena.tiendavirtual.service.BusinessException;
import co.edu.sena.tiendavirtual.service.CartService;
import co.edu.sena.tiendavirtual.service.CurrentUserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** RF-023 (agregar), RF-024 (modificar) y RF-025 (consultar carrito). */
@Controller
@RequestMapping("/carrito")
public class CartController {

    private final CartService carts;
    private final CurrentUserService current;

    public CartController(CartService carts, CurrentUserService current) {
        this.carts = carts;
        this.current = current;
    }

    @GetMapping
    public String view(Authentication auth, Model model) {
        model.addAttribute("cart", carts.cartOf(current.customer(auth)));
        return "carrito";
    }

    @PostMapping("/agregar")
    public String add(@RequestParam Integer productId, @RequestParam(defaultValue = "1") int quantity,
                      @RequestParam(defaultValue = "/catalogo") String back,
                      Authentication auth, RedirectAttributes flash) {
        try {
            carts.add(current.customer(auth), productId, quantity);
            flash.addFlashAttribute("ok", "Producto agregado al carrito");
        } catch (BusinessException ex) {
            flash.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:" + safe(back);
    }

    @PostMapping("/actualizar")
    public String update(@RequestParam Integer productId, @RequestParam int quantity,
                         Authentication auth, RedirectAttributes flash) {
        try {
            carts.update(current.customer(auth), productId, quantity);
            flash.addFlashAttribute("ok", quantity <= 0 ? "Producto eliminado del carrito" : "Cantidad actualizada");
        } catch (BusinessException ex) {
            flash.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/carrito";
    }

    @PostMapping("/quitar")
    public String remove(@RequestParam Integer productId, Authentication auth, RedirectAttributes flash) {
        carts.remove(current.customer(auth), productId);
        flash.addFlashAttribute("ok", "Producto eliminado del carrito");
        return "redirect:/carrito";
    }

    /** Solo se permite volver a rutas internas. */
    private static String safe(String back) {
        return back.startsWith("/") && !back.startsWith("//") ? back : "/catalogo";
    }
}
