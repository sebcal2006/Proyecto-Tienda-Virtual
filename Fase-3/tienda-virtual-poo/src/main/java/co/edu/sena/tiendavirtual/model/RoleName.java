package co.edu.sena.tiendavirtual.model;

/** Los 6 roles del sistema (tabla roles, columna name). */
public enum RoleName {
    ADMIN("Administrador"),
    SELLER("Vendedor"),
    INVENTORY_MANAGER("Encargado de inventario"),
    BILLING_MANAGER("Encargado de facturación"),
    CUSTOMER("Cliente"),
    COURIER("Domiciliario");

    private final String label;

    RoleName(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
