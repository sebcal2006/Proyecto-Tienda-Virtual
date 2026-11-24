package co.edu.sena.tiendavirtual.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MoneyFormatterTest {

    private final MoneyFormatter fmt = new MoneyFormatter();

    @Test
    void formateaPesosColombianos() {
        assertEquals("$ 2.500.000", fmt.money(new BigDecimal("2500000.00")));
        assertEquals("$ 85.000", fmt.money(new BigDecimal("85000")));
        assertEquals("$ 0", fmt.money(null));
    }
}
