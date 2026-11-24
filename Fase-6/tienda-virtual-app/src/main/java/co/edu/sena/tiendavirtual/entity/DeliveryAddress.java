package co.edu.sena.tiendavirtual.entity;

import jakarta.persistence.*;

/** Tabla delivery_addresses. */
@Entity
@Table(name = "delivery_addresses")
public class DeliveryAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 200)
    private String address;

    @Column(nullable = false, length = 80)
    private String city;

    @Column(length = 200)
    private String reference;

    protected DeliveryAddress() {
    }

    public DeliveryAddress(String address, String city, String reference) {
        this.address = address;
        this.city = city;
        this.reference = reference;
    }

    public Integer getId() { return id; }
    public String getAddress() { return address; }
    public String getCity() { return city; }
    public String getReference() { return reference; }
}
