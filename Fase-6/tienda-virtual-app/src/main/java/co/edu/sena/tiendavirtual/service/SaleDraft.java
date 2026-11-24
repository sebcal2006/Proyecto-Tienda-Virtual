package co.edu.sena.tiendavirtual.service;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Venta presencial en curso (RF-019 a RF-022). Vive en la sesión del vendedor
 * hasta que la confirma y se guarda como pedido IN_STORE. RN-007: mientras no
 * se confirme se puede modificar.
 */
public class SaleDraft implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer customerId;
    private final Map<Integer, Integer> lines = new LinkedHashMap<>();

    public Integer getCustomerId() { return customerId; }
    public void setCustomerId(Integer customerId) { this.customerId = customerId; }
    public Map<Integer, Integer> getLines() { return lines; }

    public boolean isEmpty() {
        return lines.isEmpty();
    }
}
