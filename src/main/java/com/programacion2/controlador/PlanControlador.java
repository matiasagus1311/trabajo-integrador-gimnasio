package com.programacion2.controlador;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.programacion2.excepcion.ServicioException;
import com.programacion2.servicio.IServicioPlan;

/** Los planes solo se consultan (la consigna pide consultar la 3ra entidad). */
@Controller
@RequestMapping("/empleado/planes")
public class PlanControlador {

    private final IServicioPlan servicioPlan;

    @Autowired
    public PlanControlador(IServicioPlan servicioPlan) {
        this.servicioPlan = servicioPlan;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("planes", servicioPlan.listar());
        return "empleado/planes";
    }

    @GetMapping("/{id}")
    public String ficha(@PathVariable int id, Model model, RedirectAttributes redirect) {
        try {
            model.addAttribute("plan", servicioPlan.consultar(id));
            return "empleado/plan-ficha";
        } catch (ServicioException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/empleado/planes";
        }
    }
}
