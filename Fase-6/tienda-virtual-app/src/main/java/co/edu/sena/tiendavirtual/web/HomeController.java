package co.edu.sena.tiendavirtual.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "redirect:/catalogo";
    }

    /** Usuarios con roles que aún no tienen módulo en esta entrega (inventario, facturación, domicilios). */
    @GetMapping("/panel")
    public String panel() {
        return "panel";
    }

    /** Acepta GET y POST: Spring Security reenvía aquí la petición original cuando el rol no tiene permiso. */
    @RequestMapping("/acceso-denegado")
    public String denied() {
        return "error/403";
    }
}
