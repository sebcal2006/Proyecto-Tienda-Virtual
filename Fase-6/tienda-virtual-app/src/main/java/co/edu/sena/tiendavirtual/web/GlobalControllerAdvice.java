package co.edu.sena.tiendavirtual.web;

import co.edu.sena.tiendavirtual.entity.Role;
import co.edu.sena.tiendavirtual.repository.CartRepository;
import co.edu.sena.tiendavirtual.repository.CustomerRepository;
import co.edu.sena.tiendavirtual.repository.UserRepository;
import co.edu.sena.tiendavirtual.service.BusinessException;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;

/** Datos comunes de la barra superior y página para errores de negocio en consultas. */
@ControllerAdvice
public class GlobalControllerAdvice {

    private final UserRepository users;
    private final CustomerRepository customers;
    private final CartRepository carts;

    public GlobalControllerAdvice(UserRepository users, CustomerRepository customers, CartRepository carts) {
        this.users = users;
        this.customers = customers;
        this.carts = carts;
    }

    /** Unidades en el carrito para el contador del menú del cliente. */
    @ModelAttribute("cartUnits")
    public int cartUnits(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) return 0;
        return users.findByEmailIgnoreCase(auth.getName())
                .filter(u -> u.hasRole(Role.CUSTOMER))
                .flatMap(customers::findByUser)
                .flatMap(carts::findByCustomer)
                .map(c -> c.getUnits())
                .orElse(0);
    }

    @ModelAttribute("userName")
    public String userName(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) return null;
        return users.findByEmailIgnoreCase(auth.getName()).map(u -> u.getName()).orElse(auth.getName());
    }

    @ExceptionHandler(BusinessException.class)
    public String businessError(BusinessException ex, Model model) {
        model.addAttribute("message", ex.getMessage());
        return "error/negocio";
    }
}
