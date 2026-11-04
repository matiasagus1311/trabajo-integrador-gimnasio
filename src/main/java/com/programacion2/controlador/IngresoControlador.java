package com.programacion2.controlador;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.programacion2.dominio.Actividad;
import com.programacion2.dominio.DiaSemana;
import com.programacion2.dominio.Ingreso;
import com.programacion2.excepcion.ServicioException;
import com.programacion2.servicio.IServicioActividad;
import com.programacion2.servicio.IServicioIngreso;

/** Registro de ingreso por DNI (en recepción). */
@Controller
@RequestMapping("/empleado/ingresos")
public class IngresoControlador {

    private final IServicioIngreso servicioIngreso;
    private final IServicioActividad servicioActividad;

    @Autowired
    public IngresoControlador(IServicioIngreso servicioIngreso, IServicioActividad servicioActividad) {
        this.servicioIngreso = servicioIngreso;
        this.servicioActividad = servicioActividad;
    }

    @GetMapping
    public String pantalla(Model model) {
        model.addAttribute("ingresos", servicioIngreso.ingresosDeHoy());
        return "empleado/ingresos";
    }

    @PostMapping
    public String registrar(@RequestParam(defaultValue = "") String dni, Model model) {
        try {
            Ingreso ingreso = servicioIngreso.registrar(dni);
            DiaSemana hoy = DiaSemana.de(LocalDate.now().getDayOfWeek());
            List<Actividad> deHoy = new ArrayList<>();
            for (Actividad a : servicioActividad.actividadesDeSocio(ingreso.getSocio().getId())) {
                if (a.getDia() == hoy) {
                    deHoy.add(a);
                }
            }
            model.addAttribute("ingreso", ingreso);
            model.addAttribute("actividadesHoy", deHoy);
        } catch (ServicioException e) {
            model.addAttribute("rechazo", e.getMessage());
            model.addAttribute("dni", dni);
        }
        model.addAttribute("ingresos", servicioIngreso.ingresosDeHoy());
        return "empleado/ingresos";
    }
}
