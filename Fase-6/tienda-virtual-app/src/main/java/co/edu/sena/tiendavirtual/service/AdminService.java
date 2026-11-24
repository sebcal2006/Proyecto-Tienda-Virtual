package co.edu.sena.tiendavirtual.service;

import co.edu.sena.tiendavirtual.entity.*;
import co.edu.sena.tiendavirtual.repository.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Épica 1 (usuarios y roles: RF-002, RF-003) y épica 2 (productos: RF-005 a RF-007). */
@Service
public class AdminService {

    private final UserRepository users;
    private final RoleRepository roles;
    private final CustomerRepository customers;
    private final ProductRepository products;
    private final CategoryRepository categories;
    private final PasswordEncoder encoder;

    public AdminService(UserRepository users, RoleRepository roles, CustomerRepository customers,
                        ProductRepository products, CategoryRepository categories, PasswordEncoder encoder) {
        this.users = users;
        this.roles = roles;
        this.customers = customers;
        this.products = products;
        this.categories = categories;
        this.encoder = encoder;
    }

    // ------------------------------------------------------------------ usuarios

    /** Datos del formulario de usuario. */
    public record UserData(String name, String email, String password, String phone, boolean active,
                           List<Integer> roleIds) {
    }

    /** RF-002 CA-01 / CA-02 y RF-003. */
    @Transactional
    public User createUser(UserData data) {
        required(data.name(), "nombre");
        required(data.email(), "correo electrónico");
        required(data.password(), "contraseña");
        String email = data.email().trim().toLowerCase();
        if (users.existsByEmailIgnoreCase(email)) {
            throw new BusinessException("Este correo ya está registrado");
        }
        if (!PasswordPolicy.isValid(data.password())) {
            throw new BusinessException("La contraseña no cumple los requisitos de seguridad");
        }
        Set<Role> selected = resolveRoles(data.roleIds());
        User user = new User();
        user.setName(data.name().trim());
        user.setEmail(email);
        user.setPhone(blankToNull(data.phone()));
        user.setActive(data.active());
        user.setPasswordHash(encoder.encode(data.password()));
        user.getRoles().addAll(selected);
        users.save(user);
        ensureCustomer(user);
        return user;
    }

    /** RF-002 (modificar y desactivar) y RF-003 (asignar roles). La contraseña es opcional al editar. */
    @Transactional
    public User updateUser(Integer id, UserData data, User admin) {
        User user = user(id);
        required(data.name(), "nombre");
        Set<Role> selected = resolveRoles(data.roleIds());
        if (user.getId().equals(admin.getId())) {
            if (!data.active()) {
                throw new BusinessException("No puede desactivar su propia cuenta");
            }
            if (selected.stream().noneMatch(r -> r.getName().equals(Role.ADMIN))) {
                throw new BusinessException("No puede quitarse a sí mismo el rol Administrador");
            }
        }
        if (data.password() != null && !data.password().isBlank()) {
            if (!PasswordPolicy.isValid(data.password())) {
                throw new BusinessException("La contraseña no cumple los requisitos de seguridad");
            }
            user.setPasswordHash(encoder.encode(data.password()));
        }
        user.setName(data.name().trim());
        user.setPhone(blankToNull(data.phone()));
        user.setActive(data.active());
        user.getRoles().clear();
        user.getRoles().addAll(selected);
        ensureCustomer(user);
        return user;
    }

    public User user(Integer id) {
        return users.findById(id).orElseThrow(() -> new BusinessException("Usuario no encontrado"));
    }

    /** RF-003 CA-02. */
    private Set<Role> resolveRoles(List<Integer> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            throw new BusinessException("Seleccione al menos un rol");
        }
        Set<Role> selected = new HashSet<>();
        for (Integer roleId : roleIds) {
            selected.add(roles.findById(roleId)
                    .orElseThrow(() -> new BusinessException("El rol seleccionado no es válido")));
        }
        return selected;
    }

    /** Un usuario con rol Cliente necesita su registro en customers para comprar. */
    private void ensureCustomer(User user) {
        if (user.hasRole(Role.CUSTOMER) && customers.findByUser(user).isEmpty()) {
            Customer customer = new Customer();
            customer.setUser(user);
            customer.setName(user.getName());
            customer.setEmail(user.getEmail());
            customer.setPhone(user.getPhone());
            customers.save(customer);
        }
    }

    // ------------------------------------------------------------------ productos

    /** Datos del formulario de producto. */
    public record ProductData(String code, String name, String description, BigDecimal price, Integer categoryId) {
    }

    /** RF-005: el producto nuevo entra con existencia 0; las unidades llegan por entradas de inventario. */
    @Transactional
    public Product createProduct(ProductData data) {
        required(data.code(), "código");
        if (products.existsByCodeIgnoreCase(data.code().trim())) {
            throw new BusinessException("Ya existe un producto con este código");
        }
        Product product = new Product(data.code().trim().toUpperCase(), "", BigDecimal.ZERO, 0);
        apply(product, data);
        return products.save(product);
    }

    /** RF-006. El código no cambia porque identifica al producto en ventas y facturas. */
    @Transactional
    public Product updateProduct(Integer id, ProductData data) {
        Product product = product(id);
        apply(product, data);
        return product;
    }

    /** RF-007 CA-02: cuántos carritos o pedidos sin despachar incluyen el producto. */
    public long inProgressUses(Integer id) {
        return products.countInProgressUses(id);
    }

    /** RF-007: un producto inactivo deja de venderse en ambos canales. */
    @Transactional
    public Product setActive(Integer id, boolean active) {
        Product product = product(id);
        product.setActive(active);
        return product;
    }

    public Product product(Integer id) {
        return products.findById(id).orElseThrow(() -> new BusinessException("Producto no encontrado"));
    }

    private void apply(Product product, ProductData data) {
        required(data.name(), "nombre");
        if (data.price() == null) {
            throw new BusinessException("Falta el campo obligatorio: precio");
        }
        if (data.price().signum() < 0) {
            throw new BusinessException("El precio no puede ser negativo");
        }
        if (data.price().signum() == 0) {
            throw new BusinessException("El precio debe ser mayor que cero");
        }
        Category category = data.categoryId() == null ? null : categories.findById(data.categoryId()).orElse(null);
        if (category == null) {
            throw new BusinessException("Seleccione una categoría válida");
        }
        product.setName(data.name().trim());
        product.setDescription(blankToNull(data.description()));
        product.setPrice(data.price());
        product.setCategory(category);
    }

    private static void required(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new BusinessException("Falta el campo obligatorio: " + field);
        }
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
