package co.edu.sena.tiendavirtual.model;

/**
 * Estados del pedido (orders.status) y sus transiciones.
 * RN-004: el estado solo avanza PENDING -> IN_PREPARATION -> ON_THE_WAY -> DELIVERED.
 * RN-003: solo se cancela desde PENDING o IN_PREPARATION.
 */
public enum OrderStatus {
    PENDING("pendiente"),
    IN_PREPARATION("en preparación"),
    ON_THE_WAY("en camino"),
    DELIVERED("entregado"),
    CANCELLED("cancelado");

    private final String label;

    OrderStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    /** Siguiente estado permitido, o {@code null} si el estado es final. */
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
}
