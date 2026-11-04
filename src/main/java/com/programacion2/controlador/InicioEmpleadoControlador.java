package com.programacion2.controlador;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.programacion2.dominio.DiaSemana;
import com.programacion2.servicio.IServicioActividad;
import com.programacion2.servicio.IServicioIngreso;
import com.programacion2.servicio.IServicioSocio;

/** Pantalla de inicio del empleado: resumen del día. */
@Controller
public class InicioEmpleadoControlador {

    private final IServicioSocio servicioSocio;
    private final IServicioActividad servicioActividad;
    private final IServicioIngreso servicioIngreso;

    @Autowired
    public InicioEmpleadoControlador(IServicioSocio servicioSocio,
                                     IServicioActividad servicioActividad,
                                     IServicioIngreso servicioIngreso) {
        this.servicioSocio = servicioSocio;
        this.servicioActividad = servicioActividad;
        this.servicioIngreso = servicioIngreso;
    }

    @GetMapping("/empleado")
    public String inicio(Model model) {
        DiaSemana hoy = DiaSemana.de(LocalDate.now().getDayOfWeek());
        model.addAttribute("hoy", hoy);
        model.addAttribute("sociosActivos", servicioSocio.buscar("", 0, "").size());
        model.addAttribute("abonosVencidos", servicioSocio.contarAbonosVencidos());
        model.addAttribute("ingresosHoy", servicioIngreso.ingresosDeHoy());
        model.addAttribute("actividadesHoy", servicioActividad.listar(hoy));
        return "empleado/inicio";
    }
}
