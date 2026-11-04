package com.programacion2.controlador;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.programacion2.configuracion.ControlSesion;
import com.programacion2.dominio.Actividad;
import com.programacion2.excepcion.ServicioException;
import com.programacion2.servicio.IServicioActividad;
import com.programacion2.servicio.IServicioIngreso;
import com.programacion2.servicio.IServicioSocio;
import com.programacion2.servicio.UsuarioSesion;

/** Portal del socio: su abono, sus actividades y su cuenta. Siempre trabaja con el socio de la sesión. */
@Controller
@RequestMapping("/socio")
public class PortalSocioControlador {

    private final IServicioSocio servicioSocio;
    private final IServicioActividad servicioActividad;
    private final IServicioIngreso servicioIngreso;

    @Autowired
    public PortalSocioControlador(IServicioSocio servicioSocio, IServicioActividad servicioActividad,
                                  IServicioIngreso servicioIngreso) {
        this.servicioSocio = servicioSocio;
        this.servicioActividad = servicioActividad;
        this.servicioIngreso = servicioIngreso;
    }

    @GetMapping
    public String miAbono(@SessionAttribute(ControlSesion.USUARIO) UsuarioSesion usuario, Model model) {
        model.addAttribute("socio", servicioSocio.consultar(usuario.getId()));
        model.addAttribute("actividades", servicioActividad.actividadesDeSocio(usuario.getId()));
        model.addAttribute("ingresos", servicioIngreso.ingresosDeSocio(usuario.getId()));
        return "socio/abono";
    }

    @GetMapping("/actividades")
    public String actividades(@SessionAttribute(ControlSesion.USUARIO) UsuarioSesion usuario, Model model) {
        List<Actividad> mias = servicioActividad.actividadesDeSocio(usuario.getId());
        model.addAttribute("socio", servicioSocio.consultar(usuario.getId()));
        model.addAttribute("mias", mias);
        model.addAttribute("actividades", servicioActividad.listarActivas());
        return "socio/actividades";
    }

    @PostMapping("/actividades/{id}/anotarme")
    public String anotarme(@SessionAttribute(ControlSesion.USUARIO) UsuarioSesion usuario,
                           @PathVariable int id, RedirectAttributes redirect) {
        try {
            servicioActividad.inscribir(id, usuario.getId());
            redirect.addFlashAttribute("exito", "Te anotaste en la actividad.");
        } catch (ServicioException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/socio/actividades";
    }

    @PostMapping("/actividades/{id}/baja")
    public String darmeDeBaja(@SessionAttribute(ControlSesion.USUARIO) UsuarioSesion usuario,
                              @PathVariable int id, RedirectAttributes redirect) {
        try {
            servicioActividad.desinscribir(id, usuario.getId());
            redirect.addFlashAttribute("exito", "Te diste de baja de la actividad.");
        } catch (ServicioException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/socio/actividades";
    }

    @GetMapping("/cuenta")
    public String cuenta(@SessionAttribute(ControlSesion.USUARIO) UsuarioSesion usuario, Model model) {
        model.addAttribute("socio", servicioSocio.consultar(usuario.getId()));
        return "socio/cuenta";
    }

    @PostMapping("/cuenta")
    public String cambiarClave(@SessionAttribute(ControlSesion.USUARIO) UsuarioSesion usuario,
                               @RequestParam String actual, @RequestParam String nueva,
                               @RequestParam String repetida, RedirectAttributes redirect) {
        try {
            servicioSocio.cambiarClave(usuario.getId(), actual, nueva, repetida);
            redirect.addFlashAttribute("exito", "Tu clave se cambió.");
        } catch (ServicioException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/socio/cuenta";
    }
}
