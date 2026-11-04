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
import com.programacion2.dominio.MedioPago;
import com.programacion2.dominio.Pago;
import com.programacion2.dominio.Socio;
import com.programacion2.excepcion.ServicioException;
import com.programacion2.servicio.IServicioActividad;
import com.programacion2.servicio.IServicioIngreso;
import com.programacion2.servicio.IServicioPago;
import com.programacion2.servicio.IServicioPlan;
import com.programacion2.servicio.IServicioSocio;

import jakarta.validation.Valid;

/** Pantallas del empleado para los socios: listado, alta, modificación, ficha, baja, cobro e inscripción. */
@Controller
@RequestMapping("/empleado/socios")
public class SocioControlador {

    private final IServicioSocio servicioSocio;
    private final IServicioPlan servicioPlan;
    private final IServicioActividad servicioActividad;
    private final IServicioPago servicioPago;
    private final IServicioIngreso servicioIngreso;

    @Autowired
    public SocioControlador(IServicioSocio servicioSocio, IServicioPlan servicioPlan,
                            IServicioActividad servicioActividad, IServicioPago servicioPago,
                            IServicioIngreso servicioIngreso) {
        this.servicioSocio = servicioSocio;
        this.servicioPlan = servicioPlan;
        this.servicioActividad = servicioActividad;
        this.servicioPago = servicioPago;
        this.servicioIngreso = servicioIngreso;
    }

    // ---------- Listado con búsqueda y filtros ----------

    @GetMapping
    public String listar(@RequestParam(defaultValue = "") String texto,
                         @RequestParam(defaultValue = "0") int planId,
                         @RequestParam(defaultValue = "") String estado,
                         Model model) {
        model.addAttribute("socios", servicioSocio.buscar(texto, planId, estado));
        model.addAttribute("planes", servicioPlan.listar());
        model.addAttribute("texto", texto);
        model.addAttribute("planId", planId);
        model.addAttribute("estado", estado);
        return "empleado/socios";
    }

    // ---------- Alta ----------

    @GetMapping("/nuevo")
    public String formularioAlta(Model model) {
        model.addAttribute("socio", new Socio());
        return mostrarFormulario(model, 0, 0);
    }

    @PostMapping("/nuevo")
    public String alta(@Valid @ModelAttribute("socio") Socio socio, BindingResult errores,
                       @RequestParam(defaultValue = "0") int planId,
                       Model model, RedirectAttributes redirect) {
        if (planId == 0) {
            model.addAttribute("errorPlan", "Elegí un plan");
        }
        if (errores.hasErrors() || planId == 0) {
            return mostrarFormulario(model, 0, planId);
        }
        try {
            Socio nuevo = servicioSocio.alta(socio, planId);
            redirect.addFlashAttribute("exito", "Socio " + nuevo.getNombreCompleto()
                    + " dado de alta. Su clave para entrar es su DNI.");
            return "redirect:/empleado/socios/" + nuevo.getId();
        } catch (ServicioException e) {
            model.addAttribute("error", e.getMessage());
            return mostrarFormulario(model, 0, planId);
        }
    }

    // ---------- Modificación ----------

    @GetMapping("/{id}/editar")
    public String formularioEdicion(@PathVariable int id, Model model, RedirectAttributes redirect) {
        try {
            Socio socio = servicioSocio.consultar(id);
            model.addAttribute("socio", socio);
            return mostrarFormulario(model, id, socio.getPlan().getId());
        } catch (ServicioException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/empleado/socios";
        }
    }

    @PostMapping("/{id}/editar")
    public String modificar(@PathVariable int id,
                            @Valid @ModelAttribute("socio") Socio socio, BindingResult errores,
                            @RequestParam(defaultValue = "0") int planId,
                            Model model, RedirectAttributes redirect) {
        if (planId == 0) {
            model.addAttribute("errorPlan", "Elegí un plan");
        }
        if (errores.hasErrors() || planId == 0) {
            return mostrarFormulario(model, id, planId);
        }
        try {
            servicioSocio.modificar(id, socio, planId);
            redirect.addFlashAttribute("exito", "Datos del socio guardados.");
            return "redirect:/empleado/socios/" + id;
        } catch (ServicioException e) {
            model.addAttribute("error", e.getMessage());
            return mostrarFormulario(model, id, planId);
        }
    }

    private String mostrarFormulario(Model model, int id, int planId) {
        model.addAttribute("id", id);              // 0 = alta, otro = modificación
        model.addAttribute("planId", planId);
        model.addAttribute("planes", servicioPlan.listar());
        return "empleado/socio-form";
    }

    // ---------- Ficha del socio ----------

    @GetMapping("/{id}")
    public String ficha(@PathVariable int id, Model model, RedirectAttributes redirect) {
        try {
            Socio socio = servicioSocio.consultar(id);
            List<Actividad> suyas = servicioActividad.actividadesDeSocio(id);
            // Para el combo "Inscribir en...": activas, con cupo y en las que todavía no está
            List<Actividad> disponibles = new ArrayList<>();
            for (Actividad a : servicioActividad.listarActivas()) {
                if (!suyas.contains(a) && a.hayCupo()) {
                    disponibles.add(a);
                }
            }
            model.addAttribute("socio", socio);
            model.addAttribute("actividades", suyas);
            model.addAttribute("disponibles", disponibles);
            model.addAttribute("ingresos", servicioIngreso.ingresosDeSocio(id));
            return "empleado/socio-ficha";
        } catch (ServicioException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/empleado/socios";
        }
    }

    // ---------- Baja lógica y reactivación ----------

    @PostMapping("/{id}/baja")
    public String baja(@PathVariable int id, RedirectAttributes redirect) {
        try {
            servicioSocio.darDeBaja(id);
            redirect.addFlashAttribute("exito", "Socio dado de baja. Se lo quitó de sus actividades.");
        } catch (ServicioException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/empleado/socios/" + id;
    }

    @PostMapping("/{id}/reactivar")
    public String reactivar(@PathVariable int id, RedirectAttributes redirect) {
        try {
            servicioSocio.reactivar(id);
            redirect.addFlashAttribute("exito", "Socio reactivado.");
        } catch (ServicioException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/empleado/socios/" + id;
    }

    // ---------- Cobro ----------

    @GetMapping("/{id}/cobrar")
    public String formularioCobro(@PathVariable int id, Model model, RedirectAttributes redirect) {
        try {
            Pago pago = servicioPago.prepararCobro(id);   // solo para mostrar, no se guarda
            model.addAttribute("pago", pago);
            model.addAttribute("socio", pago.getSocio());
            model.addAttribute("medios", MedioPago.values());
            model.addAttribute("planes", servicioPlan.listar());
            return "empleado/cobrar";
        } catch (ServicioException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/empleado/socios/" + id;
        }
    }

    @PostMapping("/{id}/cobrar")
    public String cobrar(@PathVariable int id,
                         @RequestParam(defaultValue = "0") int planId,
                         @RequestParam(required = false) MedioPago medioPago,
                         RedirectAttributes redirect) {
        try {
            Pago pago = servicioPago.cobrar(id, planId, medioPago);
            redirect.addFlashAttribute("exito", "Cobro registrado: " + pago.getSocio().getPlan().getNombre()
                    + ". Abono vigente hasta el " + Formato.fecha(pago.getHasta()) + ".");
            return "redirect:/empleado/socios/" + id;
        } catch (ServicioException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/empleado/socios/" + id + "/cobrar";
        }
    }

    // ---------- Inscripción a actividades ----------

    @PostMapping("/{id}/inscribir")
    public String inscribir(@PathVariable int id,
                            @RequestParam(defaultValue = "0") int actividadId,
                            RedirectAttributes redirect) {
        try {
            servicioActividad.inscribir(actividadId, id);
            redirect.addFlashAttribute("exito", "Socio inscripto en la actividad.");
        } catch (ServicioException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/empleado/socios/" + id;
    }

    @PostMapping("/{id}/desinscribir")
    public String desinscribir(@PathVariable int id, @RequestParam int actividadId,
                               RedirectAttributes redirect) {
        try {
            servicioActividad.desinscribir(actividadId, id);
            redirect.addFlashAttribute("exito", "Se quitó al socio de la actividad.");
        } catch (ServicioException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/empleado/socios/" + id;
    }
}
