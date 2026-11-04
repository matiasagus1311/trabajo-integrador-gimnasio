package com.programacion2.configuracion;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.programacion2.servicio.UsuarioSesion;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Interceptor de Spring MVC: se ejecuta ANTES del controlador.
 * Si nadie inició sesión, o el rol no corresponde a la zona,
 * manda al login y el controlador no se ejecuta.
 */
@Component
public class ControlSesion implements HandlerInterceptor {

    public static final String USUARIO = "usuario";   // nombre del atributo en la sesión

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        HttpSession sesion = request.getSession(false);
        UsuarioSesion usuario = (sesion == null) ? null : (UsuarioSesion) sesion.getAttribute(USUARIO);

        String ruta = request.getRequestURI().substring(request.getContextPath().length());
        boolean zonaEmpleado = ruta.startsWith("/empleado");

        if (usuario == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return false;
        }
        if (zonaEmpleado && !usuario.esEmpleado()) {
            response.sendRedirect(request.getContextPath() + "/socio");
            return false;
        }
        if (!zonaEmpleado && usuario.esEmpleado()) {
            response.sendRedirect(request.getContextPath() + "/empleado");
            return false;
        }
        return true;
    }
}
