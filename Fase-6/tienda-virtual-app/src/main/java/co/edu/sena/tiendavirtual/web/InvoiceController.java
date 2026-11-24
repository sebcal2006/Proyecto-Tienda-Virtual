package co.edu.sena.tiendavirtual.web;

import co.edu.sena.tiendavirtual.entity.Invoice;
import co.edu.sena.tiendavirtual.service.BusinessException;
import co.edu.sena.tiendavirtual.service.CurrentUserService;
import co.edu.sena.tiendavirtual.service.InvoiceService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Épica 10: generar (RF-041) y consultar facturas (RF-042). El administrador solo consulta. */
@Controller
@RequestMapping("/facturacion")
public class InvoiceController {

    private final InvoiceService invoices;
    private final CurrentUserService current;

    public InvoiceController(InvoiceService invoices, CurrentUserService current) {
        this.invoices = invoices;
        this.current = current;
    }

    @GetMapping
    public String index(Model model) {
        model.addAttribute("pending", invoices.pending());
        model.addAttribute("invoices", invoices.all());
        return "facturacion/index";
    }

    @GetMapping("/{id}")
    public String invoice(@PathVariable Integer id, Model model) {
        model.addAttribute("invoice", invoices.get(id));
        return "facturacion/factura";
    }

    @PostMapping("/generar")
    public String generate(@RequestParam Integer orderId, Authentication auth, RedirectAttributes flash) {
        try {
            Invoice invoice = invoices.generate(orderId, current.user(auth));
            flash.addFlashAttribute("ok", "Factura " + invoice.getNumber() + " generada");
            return "redirect:/facturacion/" + invoice.getId();
        } catch (BusinessException ex) {
            flash.addFlashAttribute("error", ex.getMessage());
            return "redirect:/facturacion";
        }
    }
}
