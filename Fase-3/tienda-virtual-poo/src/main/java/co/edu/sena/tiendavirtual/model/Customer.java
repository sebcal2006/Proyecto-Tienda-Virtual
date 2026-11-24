package co.edu.sena.tiendavirtual.model;

import co.edu.sena.tiendavirtual.exception.BusinessRuleException;

import java.time.LocalDateTime;

/**
 * Cliente de la tienda (tabla customers). Puede tener cuenta propia (canal en
 * línea, RF-004) o ser registrado por el vendedor sin cuenta (RF-015).
 */
public class Customer {

    private Integer id;
    private final User user;          // 0..1: null si lo registró el vendedor
    private String document;
    private String name;
    private String phone;
    private String email;
    private final LocalDateTime createdAt = LocalDateTime.now();

    /** RF-004: cliente con cuenta propia. */
    public Customer(User user) {
        if (user == null || !user.hasRole(RoleName.CUSTOMER)) {
            throw new BusinessRuleException("RF-004", "La cuenta debe tener el rol Cliente");
        }
        this.user = user;
        this.name = user.getName();
        this.email = user.getEmail();
        this.phone = user.getPhone();
    }

    /** RF-015: cliente registrado por el vendedor, sin cuenta. */
    public Customer(String document, String name, String phone) {
        this.user = null;
        updateData(document, name, phone, null);
    }

    /** RF-017. */
    public void updateData(String document, String name, String phone, String email) {
        if (name == null || name.isBlank()) {
            throw new BusinessRuleException("RF-017", "El nombre del cliente es obligatorio");
        }
        this.document = document;
        this.name = name.trim();
        this.phone = phone;
        this.email = email;
    }

    public boolean hasAccount() { return user != null; }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public User getUser() { return user; }
    public String getDocument() { return document; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    @Override
    public String toString() {
        return name + (document != null ? " (" + document + ")" : "");
    }
}
