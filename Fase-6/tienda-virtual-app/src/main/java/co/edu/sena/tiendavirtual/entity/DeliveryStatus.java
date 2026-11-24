package co.edu.sena.tiendavirtual.entity;

/** Estados del domicilio (deliveries.status). */
public enum DeliveryStatus {
    ASSIGNED("Asignado", "badge-gray"),
    ON_THE_WAY("En camino", "badge-blue"),
    DELIVERED("Entregado", "badge-green");

    private final String label;
    private final String css;

    DeliveryStatus(String label, String css) {
        this.label = label;
        this.css = css;
    }

    public String getLabel() { return label; }
    public String getCss() { return css; }
}
