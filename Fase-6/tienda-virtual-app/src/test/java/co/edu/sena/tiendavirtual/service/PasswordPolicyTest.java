package co.edu.sena.tiendavirtual.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** RF-004 E2. */
class PasswordPolicyTest {

    @Test
    void aceptaOchoCaracteresConLetrasYNumeros() {
        assertTrue(PasswordPolicy.isValid("Cliente123"));
    }

    @Test
    void rechazaContrasenasDebiles() {
        assertFalse(PasswordPolicy.isValid(null));
        assertFalse(PasswordPolicy.isValid("abc123"));      // corta
        assertFalse(PasswordPolicy.isValid("soloLetras"));  // sin números
        assertFalse(PasswordPolicy.isValid("12345678"));    // sin letras
    }
}
