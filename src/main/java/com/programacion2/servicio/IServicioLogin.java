package com.programacion2.servicio;

public interface IServicioLogin {

    /** Valida DNI y clave. Si están bien devuelve quién es y su rol; si no, ServicioException. */
    UsuarioSesion autenticar(String dni, String clave);
}
