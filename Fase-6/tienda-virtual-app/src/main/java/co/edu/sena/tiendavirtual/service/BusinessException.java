package co.edu.sena.tiendavirtual.service;

/** Error de negocio cuyo mensaje se muestra al usuario (textos de los criterios de aceptación). */
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public BusinessException(String message) {
        super(message);
    }
}
