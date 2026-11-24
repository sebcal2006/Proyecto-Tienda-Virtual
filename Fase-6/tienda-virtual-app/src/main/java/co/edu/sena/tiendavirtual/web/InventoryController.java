package co.edu.sena.tiendavirtual.web;

import co.edu.sena.tiendavirtual.entity.Product;
import co.edu.sena.tiendavirtual.repository.ProductRepository;
import co.edu.sena.tiendavirtual.service.BusinessException;
import co.edu.sena.tiendavirtual.service.CurrentUserService;
import co.edu.sena.tiendavirtual.service.InventoryService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/** Épica 3: consulta (RF-013), entradas (RF-011), salidas (RF-012) e historial (RF-014). */
@Controller
@RequestMapping("/inventario")
public class InventoryController {

    private final InventoryService inventory;
    private final ProductRepository products;
    private final CurrentUserService current;

    public InventoryController(InventoryService inventory, ProductRepository products, CurrentUserService current) {
        this.inventory = inventory;
        this.products = products;
        this.current = current;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String q,
                       @RequestParam(defaultValue = "false") boolean bajo, Model model) {
        String query = q == null || q.isBlank() ? null : q.trim();
        List<Product> list = products.searchAll(query);
        if (bajo) {
            list = list.stream().filter(p -> p.getStock() <= 10).toList();
        }
        model.addAttribute("products", list);
        model.addAttribute("q", query);
        model.addAttribute("bajo", bajo);
        model.addAttribute("latest", inventory.latest());
        model.addAttribute("units", list.stream().mapToInt(Product::getStock).sum());
        return "inventario/index";
    }

    @GetMapping("/{id}")
    public String product(@PathVariable Integer id, Model model) {
        Product product = products.findById(id).orElseThrow(() -> new BusinessException("Producto no encontrado"));
        model.addAttribute("product", product);
        model.addAttribute("movements", inventory.history(product));
        return "inventario/producto";
    }

    @PostMapping("/{id}/entrada")
    public String entry(@PathVariable Integer id, @RequestParam(defaultValue = "0") int quantity,
                        @RequestParam(required = false) String notes,
                        Authentication auth, RedirectAttributes flash) {
        try {
            inventory.registerEntry(id, quantity, notes, current.user(auth));
            flash.addFlashAttribute("ok", "Entrada registrada: +" + quantity + " unidades");
        } catch (BusinessException ex) {
            flash.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/inventario/" + id;
    }

    @PostMapping("/{id}/salida")
    public String exit(@PathVariable Integer id, @RequestParam(defaultValue = "0") int quantity,
                       @RequestParam(required = false) String notes,
                       Authentication auth, RedirectAttributes flash) {
        try {
            inventory.registerExit(id, quantity, notes, current.user(auth));
            flash.addFlashAttribute("ok", "Salida registrada: -" + quantity + " unidades");
        } catch (BusinessException ex) {
            flash.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/inventario/" + id;
    }
}
