package co.edu.sena.tiendavirtual.service;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/** Formato de pesos colombianos: $ 2.500.000. Se usa en las vistas como ${@fmt.money(valor)}. */
@Component("fmt")
public class MoneyFormatter {

    public String money(BigDecimal value) {
        if (value == null) return "$ 0";
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.forLanguageTag("es-CO"));
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');
        DecimalFormat format = new DecimalFormat("#,##0", symbols);
        return "$ " + format.format(value);
    }
}
