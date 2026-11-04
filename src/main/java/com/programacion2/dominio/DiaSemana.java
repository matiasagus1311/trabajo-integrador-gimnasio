package com.programacion2.dominio;

import java.time.DayOfWeek;

/** Días en que se dicta una actividad. */
public enum DiaSemana {
    LUNES("Lunes"), MARTES("Martes"), MIERCOLES("Miércoles"), JUEVES("Jueves"),
    VIERNES("Viernes"), SABADO("Sábado"), DOMINGO("Domingo");

    private final String texto;

    DiaSemana(String texto) { this.texto = texto; }

    public String getTexto() { return texto; }

    /** Convierte el día de Java (DayOfWeek) al nuestro. LUNES es el 1. */
    public static DiaSemana de(DayOfWeek dia) {
        return values()[dia.getValue() - 1];
    }
}
