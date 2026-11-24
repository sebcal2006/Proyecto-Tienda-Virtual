package co.edu.sena.tiendavirtual.service;

/** RF-004 E2: mínimo 8 caracteres, con al menos una letra y un número. */
public final class PasswordPolicy {

    private PasswordPolicy() {
    }

    public static boolean isValid(String password) {
        return password != null
                && password.length() >= 8
                && password.chars().anyMatch(Character::isLetter)
                && password.chars().anyMatch(Character::isDigit);
    }
}
