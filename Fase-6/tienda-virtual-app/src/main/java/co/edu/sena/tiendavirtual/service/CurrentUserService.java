package co.edu.sena.tiendavirtual.service;

import co.edu.sena.tiendavirtual.entity.Customer;
import co.edu.sena.tiendavirtual.entity.User;
import co.edu.sena.tiendavirtual.repository.CustomerRepository;
import co.edu.sena.tiendavirtual.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

/** Obtiene el usuario y el cliente de la sesión actual. */
@Service
public class CurrentUserService {

    private final UserRepository users;
    private final CustomerRepository customers;

    public CurrentUserService(UserRepository users, CustomerRepository customers) {
        this.users = users;
        this.customers = customers;
    }

    public User user(Authentication auth) {
        return users.findByEmailIgnoreCase(auth.getName())
                .orElseThrow(() -> new BusinessException("Usuario no encontrado"));
    }

    public Customer customer(Authentication auth) {
        User user = user(auth);
        return customers.findByUser(user)
                .orElseThrow(() -> new BusinessException("La cuenta no tiene datos de cliente"));
    }
}
