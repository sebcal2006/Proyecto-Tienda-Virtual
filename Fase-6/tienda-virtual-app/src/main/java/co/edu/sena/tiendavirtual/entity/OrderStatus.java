package co.edu.sena.tiendavirtual.entity;

/** Estados del pedido (orders.status). RN-003 y RN-004. */
public enum OrderStatus {
    PENDING("Pendiente", "badge-gray"),
    IN_PREPARATION("En preparación", "badge-amber"),
    ON_THE_WAY("En camino", "badge-blue"),
    DELIVERED("Entregado", "badge-green"),
    CANCELLED("Cancelado", "badge-red");

    private final String label;
    private final String css;

    OrderStatus(String label, String css) {
        this.label = label;
        this.css = css;
    }

    public String getLabel() { return label; }
    public String getCss() { return css; }

    /** RN-004: siguiente estado permitido o null si es final. */
    public OrderStatus next() {
        return switch (this) {
            case PENDING -> IN_PREPARATION;
            case IN_PREPARATION -> ON_THE_WAY;
            case ON_THE_WAY -> DELIVERED;
            case DELIVERED, CANCELLED -> null;
        };
    }

    /** RN-003. */
    public boolean canBeCancelled() {
        return this == PENDING || this == IN_PREPARATION;
    }

    /** Posición en la línea de seguimiento (0 a 3); -1 si está cancelado. */
    public int step() {
        return this == CANCELLED ? -1 : ordinal();
    }
}
