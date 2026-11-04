package com.programacion2.servicio;

import java.io.Serializable;

/**
 * Lo que se guarda en la sesión cuando alguien inicia sesión:
 * quién es y con qué rol. No es una clase persistente.
 */
public class UsuarioSesion implements Serializable {

    public enum Rol { EMPLEADO, SOCIO }

    private final int id;
    private final String nombre;
    private final Rol rol;

    public UsuarioSesion(int id, String nombre, Rol rol) {
        this.id = id;
        this.nombre = nombre;
        this.rol = rol;
    }

    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public Rol getRol() { return rol; }

    public boolean esEmpleado() {
        return rol == Rol.EMPLEADO;
    }
}
