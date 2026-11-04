package com.programacion2.controlador;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/** Formato de fechas para los mensajes (dd/MM/yyyy). */
public final class Formato {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private Formato() { }

    public static String fecha(LocalDate fecha) {
        return fecha == null ? "-" : fecha.format(FECHA);
    }
}
