package com.programacion2.controlador;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.programacion2.configuracion.ControlSesion;
import com.programacion2.excepcion.ServicioException;
import com.programacion2.servicio.IServicioLogin;
import com.programacion2.servicio.UsuarioSesion;

import jakarta.servlet.http.HttpSession;

@Controller
public class LoginControlador {

    private final IServicioLogin servicioLogin;

    @Autowired
    public LoginControlador(IServicioLogin servicioLogin) {
        this.servicioLogin = servicioLogin;
    }

    /** La raíz manda a cada uno a su zona (o al login). */
    @GetMapping("/")
    public String inicio(HttpSession sesion) {
        UsuarioSesion usuario = (UsuarioSesion) sesion.getAttribute(ControlSesion.USUARIO);
        if (usuario == null) {
            return "redirect:/login";
        }
        return usuario.esEmpleado() ? "redirect:/empleado" : "redirect:/socio";
    }

    @GetMapping("/login")
    public String formularioLogin() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String dni, @RequestParam String clave,
                        HttpSession sesion, Model model) {
        try {
            UsuarioSesion usuario = servicioLogin.autenticar(dni, clave);
            sesion.setAttribute(ControlSesion.USUARIO, usuario);
            return usuario.esEmpleado() ? "redirect:/empleado" : "redirect:/socio";
        } catch (ServicioException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("dni", dni);
            return "login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession sesion, RedirectAttributes redirect) {
        sesion.invalidate();
        redirect.addFlashAttribute("exito", "Cerraste sesión.");
        return "redirect:/login";
    }
}
