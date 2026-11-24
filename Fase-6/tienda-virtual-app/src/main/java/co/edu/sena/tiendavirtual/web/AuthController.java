package co.edu.sena.tiendavirtual.web;

import co.edu.sena.tiendavirtual.service.BusinessException;
import co.edu.sena.tiendavirtual.service.CustomerService;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/** RF-001 (inicio de sesión) y RF-004 (registro del cliente). */
@Controller
public class AuthController {

    private final CustomerService customers;

    public AuthController(CustomerService customers) {
        this.customers = customers;
    }

    @GetMapping("/login")
    public String login(Authentication auth) {
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            return "redirect:/";
        }
        return "login";
    }

    @GetMapping("/registro")
    public String registerForm() {
        return "registro";
    }

    @PostMapping("/registro")
    public String register(@RequestParam String name, @RequestParam String email,
                           @RequestParam String password, @RequestParam String confirm,
                           @RequestParam(required = false) String phone, Model model) {
        try {
            if (!password.equals(confirm)) {
                throw new BusinessException("Las contraseñas no coinciden");
            }
            customers.register(name, email, password, phone);
            return "redirect:/login?registrado";
        } catch (BusinessException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("name", name);
            model.addAttribute("email", email);
            model.addAttribute("phone", phone);
            return "registro";
        }
    }
}
