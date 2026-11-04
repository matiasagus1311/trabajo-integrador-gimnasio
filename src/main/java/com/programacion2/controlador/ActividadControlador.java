package com.programacion2.controlador;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.programacion2.dominio.Actividad;
import com.programacion2.dominio.DiaSemana;
import com.programacion2.dominio.Socio;
import com.programacion2.excepcion.ServicioException;
import com.programacion2.servicio.IServicioActividad;
import com.programacion2.servicio.IServicioSocio;

import jakarta.validation.Valid;

/** Pantallas del empleado para las actividades. */
@Controller
@RequestMapping("/empleado/actividades")
public class ActividadControlador {

    private final IServicioActividad servicioActividad;
    private final IServicioSocio servicioSocio;

    @Autowired
    public ActividadControlador(IServicioActividad servicioActividad, IServicioSocio servicioSocio) {
        this.servicioActividad = servicioActividad;
        this.servicioSocio = servicioSocio;
    }

    @GetMapping
    public String listar(@RequestParam(required = false) DiaSemana dia, Model model) {
        model.addAttribute("actividades", servicioActividad.listar(dia));
        model.addAttribute("dias", DiaSemana.values());
        model.addAttribute("dia", dia);
        return "empleado/actividades";
    }

    @GetMapping("/nueva")
    public String formularioAlta(Model model) {
        model.addAttribute("actividad", new Actividad());
        return mostrarFormulario(model, 0);
    }

    @PostMapping("/nueva")
    public String alta(@Valid @ModelAttribute("actividad") Actividad actividad, BindingResult errores,
                       Model model, RedirectAttributes redirect) {
        if (errores.hasErrors()) {
            return mostrarFormulario(model, 0);
        }
        try {
            Actividad nueva = servicioActividad.alta(actividad);
            redirect.addFlashAttribute("exito", "Actividad " + nueva.getNombre() + " creada.");
            return "redirect:/empleado/actividades/" + nueva.getId();
        } catch (ServicioException e) {
            model.addAttribute("error", e.getMessage());
            return mostrarFormulario(model, 0);
        }
    }

    @GetMapping("/{id}/editar")
    public String formularioEdicion(@PathVariable int id, Model model, RedirectAttributes redirect) {
        try {
            model.addAttribute("actividad", servicioActividad.consultar(id));
            return mostrarFormulario(model, id);
        } catch (ServicioException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/empleado/actividades";
        }
    }

    @PostMapping("/{id}/editar")
    public String modificar(@PathVariable int id,
                            @Valid @ModelAttribute("actividad") Actividad actividad, BindingResult errores,
                            Model model, RedirectAttributes redirect) {
        if (errores.hasErrors()) {
            return mostrarFormulario(model, id);
        }
        try {
            servicioActividad.modificar(id, actividad);
            redirect.addFlashAttribute("exito", "Actividad guardada.");
            return "redirect:/empleado/actividades/" + id;
        } catch (ServicioException e) {
            model.addAttribute("error", e.getMessage());
            return mostrarFormulario(model, id);
        }
    }

    private String mostrarFormulario(Model model, int id) {
        model.addAttribute("id", id);
        model.addAttribute("dias", DiaSemana.values());
        return "empleado/actividad-form";
    }

    @GetMapping("/{id}")
    public String ficha(@PathVariable int id, Model model, RedirectAttributes redirect) {
        try {
            Actividad actividad = servicioActividad.consultar(id);
            // Para el combo "Inscribir socio": socios con el abono al día que no están anotados
            List<Socio> disponibles = new ArrayList<>();
            for (Socio s : servicioSocio.listarHabilitados()) {
                if (!actividad.estaInscripto(s)) {
                    disponibles.add(s);
                }
            }
            model.addAttribute("actividad", actividad);
            model.addAttribute("disponibles", disponibles);
            return "empleado/actividad-ficha";
        } catch (ServicioException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/empleado/actividades";
        }
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable int id, RedirectAttributes redirect) {
        try {
            redirect.addFlashAttribute("exito", servicioActividad.eliminar(id));
            return "redirect:/empleado/actividades";
        } catch (ServicioException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/empleado/actividades/" + id;
        }
    }

    @PostMapping("/{id}/inscribir")
    public String inscribir(@PathVariable int id, @RequestParam(defaultValue = "0") int socioId,
                            RedirectAttributes redirect) {
        try {
            servicioActividad.inscribir(id, socioId);
            redirect.addFlashAttribute("exito", "Socio inscripto.");
        } catch (ServicioException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/empleado/actividades/" + id;
    }

    @PostMapping("/{id}/desinscribir")
    public String desinscribir(@PathVariable int id, @RequestParam int socioId,
                               RedirectAttributes redirect) {
        try {
            servicioActividad.desinscribir(id, socioId);
            redirect.addFlashAttribute("exito", "Se quitó al socio de la actividad.");
        } catch (ServicioException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/empleado/actividades/" + id;
    }
}
