package com.programacion2.excepcion;

/**
 * Error de una regla de negocio (ej.: "La actividad no tiene cupo").
 * La lanza el servicio y el controlador la muestra como mensaje rojo.
 */
public class ServicioException extends RuntimeException {

    public ServicioException(String mensaje) {
        super(mensaje);
    }
}
