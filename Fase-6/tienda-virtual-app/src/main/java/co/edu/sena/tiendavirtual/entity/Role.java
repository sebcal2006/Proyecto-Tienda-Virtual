package co.edu.sena.tiendavirtual.entity;

import jakarta.persistence.*;

/** Tabla roles: ADMIN, SELLER, INVENTORY_MANAGER, BILLING_MANAGER, CUSTOMER, COURIER. */
@Entity
@Table(name = "roles")
public class Role {

    public static final String ADMIN = "ADMIN";
    public static final String SELLER = "SELLER";
    public static final String CUSTOMER = "CUSTOMER";
    public static final String INVENTORY_MANAGER = "INVENTORY_MANAGER";
    public static final String BILLING_MANAGER = "BILLING_MANAGER";
    public static final String COURIER = "COURIER";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 30)
    private String name;

    @Column(length = 150)
    private String description;

    public Integer getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }

    /** Nombre del rol en español para las vistas. */
    public String getLabel() {
        return switch (name) {
            case ADMIN -> "Administrador";
            case SELLER -> "Vendedor";
            case INVENTORY_MANAGER -> "Encargado de inventario";
            case BILLING_MANAGER -> "Encargado de facturación";
            case CUSTOMER -> "Cliente";
            case COURIER -> "Domiciliario";
            default -> name;
        };
    }
}
