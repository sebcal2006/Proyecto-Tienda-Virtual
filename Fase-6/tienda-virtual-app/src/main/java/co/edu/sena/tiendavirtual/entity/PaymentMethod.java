package co.edu.sena.tiendavirtual.entity;

import jakarta.persistence.*;

/** Tabla payment_methods. */
@Entity
@Table(name = "payment_methods")
public class PaymentMethod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 40)
    private String name;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    public Integer getId() { return id; }
    public String getName() { return name; }
    public boolean isActive() { return active; }
}
