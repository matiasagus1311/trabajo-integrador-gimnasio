package com.programacion2.controlador;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/** Formato de fechas (dd/MM/yyyy) y de plata (54.000,00) para los mensajes. */
public final class Formato {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private Formato() { }

    public static String fecha(LocalDate fecha) {
        return fecha == null ? "-" : fecha.format(FECHA);
    }

    public static String plata(BigDecimal monto) {
        DecimalFormatSymbols simbolos = new DecimalFormatSymbols();
        simbolos.setDecimalSeparator(',');
        simbolos.setGroupingSeparator('.');
        return new DecimalFormat("#,##0.00", simbolos).format(monto);
    }
}
