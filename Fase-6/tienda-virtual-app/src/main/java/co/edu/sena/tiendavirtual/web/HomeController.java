package co.edu.sena.tiendavirtual.web;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Set;
import java.util.stream.Collectors;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "redirect:/catalogo";
    }

    /** Lleva a cada usuario al módulo de su rol. */
    @GetMapping("/panel")
    public String panel(Authentication auth) {
        String target = landing(auth);
        return target == null ? "panel" : "redirect:" + target;
    }

    @RequestMapping("/acceso-denegado")
    public String denied() {
        return "error/403";
    }

    /** Página de inicio según el rol; la usa también el inicio de sesión. */
    public static String landing(Authentication auth) {
        Set<String> roles = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).collect(Collectors.toSet());
        if (roles.contains("ROLE_ADMIN")) return "/admin";
        if (roles.contains("ROLE_SELLER")) return "/vendedor/venta";
        if (roles.contains("ROLE_INVENTORY_MANAGER")) return "/inventario";
        if (roles.contains("ROLE_BILLING_MANAGER")) return "/facturacion";
        if (roles.contains("ROLE_COURIER")) return "/domiciliario";
        if (roles.contains("ROLE_CUSTOMER")) return "/catalogo";
        return null;
    }
}
