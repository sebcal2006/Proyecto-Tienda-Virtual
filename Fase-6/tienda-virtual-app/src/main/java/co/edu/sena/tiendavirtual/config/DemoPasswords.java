package co.edu.sena.tiendavirtual.config;

import co.edu.sena.tiendavirtual.entity.Role;
import co.edu.sena.tiendavirtual.entity.User;
import co.edu.sena.tiendavirtual.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Los datos de prueba de la Fase 4 traen hashes de ejemplo ("$2a$10$Ejemplo...") que no
 * corresponden a ninguna contraseña. Al arrancar se reemplazan por hashes BCrypt reales:
 * personal de la tienda -> Tienda123, clientes -> Cliente123. Solo toca esos registros de
 * ejemplo; se desactiva con app.demo.passwords=false.
 */
@Component
@ConditionalOnProperty(name = "app.demo.passwords", havingValue = "true")
public class DemoPasswords implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoPasswords.class);
    private static final String EXAMPLE_PREFIX = "$2a$10$Ejemplo";

    private final UserRepository users;
    private final PasswordEncoder encoder;

    public DemoPasswords(UserRepository users, PasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        int updated = 0;
        for (User user : users.findAll()) {
            if (user.getPasswordHash() != null && user.getPasswordHash().startsWith(EXAMPLE_PREFIX)) {
                boolean onlyCustomer = user.getRoles().size() == 1 && user.hasRole(Role.CUSTOMER);
                user.setPasswordHash(encoder.encode(onlyCustomer ? "Cliente123" : "Tienda123"));
                users.save(user);
                updated++;
            }
        }
        if (updated > 0) {
            log.info("Contraseñas de prueba asignadas a {} usuarios (personal: Tienda123, clientes: Cliente123)", updated);
        }
    }
}
