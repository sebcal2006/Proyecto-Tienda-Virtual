package co.edu.sena.tiendavirtual.exception;

/**
 * Se lanza cuando una operación viola una regla de negocio (RN) o un
 * flujo anormal de un requerimiento funcional (RF) del ERS.
 * El mensaje es el texto que el sistema le muestra al usuario.
 */
public class BusinessRuleException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String rule;

    public BusinessRuleException(String rule, String message) {
        super(message);
        this.rule = rule;
    }

    /** Código de la regla o requerimiento que se violó, por ejemplo "RN-002". */
    public String getRule() {
        return rule;
    }
}
