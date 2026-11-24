package co.edu.sena.tiendavirtual.model;

import co.edu.sena.tiendavirtual.exception.BusinessRuleException;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Cuenta de acceso del personal y de los clientes registrados (tabla users).
 * RF-001, RF-002, RF-003. RNF-003: solo se guarda el hash de la contraseña.
 */
public class User {

    private Integer id;
    private String name;
    private String email;
    private String passwordHash;
    private String phone;
    private boolean active = true;
    private final LocalDateTime createdAt = LocalDateTime.now();
    private final Set<Role> roles = new LinkedHashSet<>();

    public User(String name, String email, String passwordHash) {
        setName(name);
        setEmail(email);
        this.passwordHash = Objects.requireNonNull(passwordHash, "El hash de la contraseña es obligatorio");
    }

    /** RF-003: un usuario puede tener uno o más roles. */
    public void assignRole(Role role) {
        if (role == null) {
            throw new BusinessRuleException("RF-003", "El rol seleccionado no es válido");
        }
        roles.add(role);
    }

    public void removeRole(Role role) {
        roles.remove(role);
    }

    public boolean hasRole(RoleName roleName) {
        return roles.stream().anyMatch(r -> r.getName() == roleName);
    }

    /**
     * RNF-002: control de acceso por rol. El administrador además puede
     * consultar (solo lectura) pedidos, inventario y facturación.
     */
    public boolean canView(RoleName module) {
        return hasRole(module) || hasRole(RoleName.ADMIN);
    }

    /** RF-001 E2: una cuenta inactiva no puede iniciar sesión. */
    public void checkCanLogIn() {
        if (!active) {
            throw new BusinessRuleException("RF-001", "Esta cuenta se encuentra inactiva");
        }
    }

    /** Lanza una excepción si el usuario no tiene el rol indicado (RNF-002). */
    public void requireRole(RoleName roleName) {
        if (!hasRole(roleName)) {
            throw new BusinessRuleException("RNF-002",
                    "El usuario no tiene el rol " + roleName.getLabel() + " para esta operación");
        }
    }

    public void deactivate() { this.active = false; }
    public void activate() { this.active = true; }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return name; }

    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new BusinessRuleException("RF-002", "El nombre es obligatorio");
        }
        this.name = name.trim();
    }

    public String getEmail() { return email; }

    public void setEmail(String email) {
        if (email == null || !email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new BusinessRuleException("RF-002", "El correo no es válido");
        }
        this.email = email.trim().toLowerCase();
    }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = Objects.requireNonNull(passwordHash); }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public boolean isActive() { return active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public Set<Role> getRoles() { return Collections.unmodifiableSet(roles); }

    @Override
    public String toString() {
        return name + " <" + email + "> " + roles;
    }
}
