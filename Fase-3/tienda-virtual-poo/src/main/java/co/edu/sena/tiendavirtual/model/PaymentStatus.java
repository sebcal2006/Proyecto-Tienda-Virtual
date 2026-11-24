package co.edu.sena.tiendavirtual.model;

/** Estado del pago (payments.status). */
public enum PaymentStatus {
    PENDING("pendiente"),
    APPROVED("aprobado"),
    REJECTED("rechazado");

    private final String label;

    PaymentStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
