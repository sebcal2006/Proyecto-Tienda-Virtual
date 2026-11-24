package co.edu.sena.tiendavirtual.entity;

/** payments.status */
public enum PaymentStatus {
    PENDING("Pendiente", "badge-gray"),
    APPROVED("Aprobado", "badge-green"),
    REJECTED("Rechazado", "badge-red");

    private final String label;
    private final String css;

    PaymentStatus(String label, String css) {
        this.label = label;
        this.css = css;
    }

    public String getLabel() { return label; }
    public String getCss() { return css; }
}
