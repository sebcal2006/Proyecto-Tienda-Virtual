package co.edu.sena.tiendavirtual.service;

import co.edu.sena.tiendavirtual.entity.Customer;
import co.edu.sena.tiendavirtual.entity.Role;
import co.edu.sena.tiendavirtual.entity.User;
import co.edu.sena.tiendavirtual.repository.CustomerRepository;
import co.edu.sena.tiendavirtual.repository.RoleRepository;
import co.edu.sena.tiendavirtual.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/** RF-004 (registro del cliente en línea) y RF-015 a RF-017 (clientes del vendedor). */
@Service
public class CustomerService {

    private final UserRepository users;
    private final RoleRepository roles;
    private final CustomerRepository customers;
    private final PasswordEncoder encoder;

    public CustomerService(UserRepository users, RoleRepository roles, CustomerRepository customers,
                           PasswordEncoder encoder) {
        this.users = users;
        this.roles = roles;
        this.customers = customers;
        this.encoder = encoder;
    }

    /** RF-004: crea la cuenta con rol Cliente y su registro en customers. */
    @Transactional
    public User register(String name, String email, String password, String phone) {
        if (isBlank(name) || isBlank(email) || isBlank(password)) {
            throw new BusinessException("Complete los campos obligatorios");
        }
        String cleanEmail = email.trim().toLowerCase();
        if (users.existsByEmailIgnoreCase(cleanEmail)) {
            throw new BusinessException("Este correo ya está registrado");
        }
        if (!PasswordPolicy.isValid(password)) {
            throw new BusinessException("La contraseña no cumple los requisitos de seguridad");
        }
        User user = new User();
        user.setName(name.trim());
        user.setEmail(cleanEmail);
        user.setPhone(blankToNull(phone));
        user.setPasswordHash(encoder.encode(password));
        user.getRoles().add(roles.findByName(Role.CUSTOMER)
                .orElseThrow(() -> new BusinessException("El rol Cliente no existe en la base de datos")));
        users.save(user);

        Customer customer = new Customer();
        customer.setUser(user);
        customer.setName(user.getName());
        customer.setEmail(user.getEmail());
        customer.setPhone(user.getPhone());
        customers.save(customer);
        return user;
    }

    /** RF-016. */
    public Optional<Customer> findByDocument(String document) {
        return isBlank(document) ? Optional.empty() : customers.findByDocument(document.trim());
    }

    /** RF-015: el vendedor registra un cliente sin cuenta. */
    @Transactional
    public Customer registerInStore(String document, String name, String phone) {
        if (isBlank(document) || isBlank(name)) {
            throw new BusinessException("El documento y el nombre son obligatorios");
        }
        if (customers.findByDocument(document.trim()).isPresent()) {
            throw new BusinessException("Ya existe un cliente con este documento");
        }
        Customer customer = new Customer();
        customer.setDocument(document.trim());
        customer.setName(name.trim());
        customer.setPhone(blankToNull(phone));
        return customers.save(customer);
    }

    public Customer get(Integer id) {
        return customers.findById(id).orElseThrow(() -> new BusinessException("Cliente no encontrado"));
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private static String blankToNull(String s) {
        return isBlank(s) ? null : s.trim();
    }
}
