package co.edu.sena.tiendavirtual.model;

import co.edu.sena.tiendavirtual.exception.BusinessRuleException;

/** Dirección de entrega del pedido (tabla delivery_addresses). RF-027. */
public class DeliveryAddress {

    private Integer id;
    private final String address;
    private final String city;
    private final String reference;

    public DeliveryAddress(String address, String city, String reference) {
        if (address == null || address.isBlank() || city == null || city.isBlank()) {
            throw new BusinessRuleException("RF-027", "La dirección y la ciudad son obligatorias");
        }
        this.address = address.trim();
        this.city = city.trim();
        this.reference = reference;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getAddress() { return address; }
    public String getCity() { return city; }
    public String getReference() { return reference; }

    @Override
    public String toString() {
        return address + ", " + city + (reference != null ? " (" + reference + ")" : "");
    }
}
