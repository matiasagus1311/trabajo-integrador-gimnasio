package com.programacion2.controlador;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.programacion2.configuracion.ControlSesion;
import com.programacion2.servicio.UsuarioSesion;

import jakarta.servlet.http.HttpSession;

/**
 * Agrega a TODAS las vistas el usuario que inició sesión,
 * para que la barra de navegación muestre su nombre.
 */
@ControllerAdvice
public class DatosComunes {

    @ModelAttribute("usuario")
    public UsuarioSesion usuario(HttpSession sesion) {
        return (UsuarioSesion) sesion.getAttribute(ControlSesion.USUARIO);
    }
}
