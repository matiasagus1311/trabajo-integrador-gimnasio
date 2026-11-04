package com.programacion2.dominio;

/** Formas de pago de la cuota. */
public enum MedioPago {
    EFECTIVO("Efectivo"), TRANSFERENCIA("Transferencia"), TARJETA("Tarjeta de débito");

    private final String texto;

    MedioPago(String texto) { this.texto = texto; }

    public String getTexto() { return texto; }
}
