package co.edu.sena.tiendavirtual.entity;

/** Canal de venta (orders.channel). */
public enum SalesChannel {
    IN_STORE("Presencial"),
    ONLINE("En línea");

    private final String label;

    SalesChannel(String label) { this.label = label; }

    public String getLabel() { return label; }
}
