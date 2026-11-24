package co.edu.sena.tiendavirtual.model;

import co.edu.sena.tiendavirtual.exception.BusinessRuleException;

/** Método de pago disponible (tabla payment_methods). RF-032. */
public class PaymentMethod {

    private Integer id;
    private final String name;
    private boolean active = true;

    public PaymentMethod(String name) {
        if (name == null || name.isBlank()) {
            throw new BusinessRuleException("RF-032", "El nombre del método de pago es obligatorio");
        }
        this.name = name.trim();
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return name; }
    public boolean isActive() { return active; }
    public void deactivate() { this.active = false; }
    public void activate() { this.active = true; }

    @Override
    public String toString() {
        return name;
    }
}
