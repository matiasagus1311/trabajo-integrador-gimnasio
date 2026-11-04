package com.programacion2.dominio;

import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;

/** Empleado del gimnasio: inicia sesión para cargar socios, cobrar y registrar ingresos. */
@PersistenceCapable(detachable = "true")
@Inheritance(strategy = InheritanceStrategy.NEW_TABLE)
public class Empleado extends Persona {

    private int legajo;

    public Empleado() { }

    public Empleado(String dni, String nombre, String apellido, String email, int legajo) {
        super(dni, nombre, apellido, email);
        this.legajo = legajo;
    }

    public int getLegajo() { return legajo; }
    public void setLegajo(int legajo) { this.legajo = legajo; }
}
