package com.programacion2.configuracion;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.programacion2.dominio.Actividad;
import com.programacion2.dominio.DiaSemana;
import com.programacion2.dominio.Empleado;
import com.programacion2.dominio.MedioPago;
import com.programacion2.dominio.Persona;
import com.programacion2.dominio.Plan;
import com.programacion2.dominio.Socio;
import com.programacion2.repositorio.IRepositorio;
import com.programacion2.servicio.IServicioActividad;
import com.programacion2.servicio.IServicioPago;
import com.programacion2.servicio.IServicioSocio;

/**
 * Carga los datos iniciales la primera vez que arranca la aplicación
 * (solo si la base no tiene planes): los 3 planes, el empleado
 * administrador y algunos datos de ejemplo para la demo.
 * Los socios, cobros e inscripciones de ejemplo pasan por los servicios,
 * así respetan las mismas reglas que la aplicación.
 */
@Component
public class DatosIniciales implements CommandLineRunner {

    private final IRepositorio<Plan> repositorioPlanes;
    private final IRepositorio<Persona> repositorioPersonas;
    private final IServicioSocio servicioSocio;
    private final IServicioActividad servicioActividad;
    private final IServicioPago servicioPago;
    private final PasswordEncoder encriptador;

    @Autowired
    public DatosIniciales(IRepositorio<Plan> repositorioPlanes,
                          IRepositorio<Persona> repositorioPersonas,
                          IServicioSocio servicioSocio,
                          IServicioActividad servicioActividad,
                          IServicioPago servicioPago,
                          PasswordEncoder encriptador) {
        this.repositorioPlanes = repositorioPlanes;
        this.repositorioPersonas = repositorioPersonas;
        this.servicioSocio = servicioSocio;
        this.servicioActividad = servicioActividad;
        this.servicioPago = servicioPago;
        this.encriptador = encriptador;
    }

    @Override
    public void run(String... args) {
        if (!repositorioPlanes.listar(Plan.class).isEmpty()) {
            return;   // ya hay datos: no se toca nada
        }
        System.out.println("Base vacía: cargando datos iniciales...");

        // Los 3 pases del TP del 1er cuatrimestre
        Plan dos = new Plan("Dos veces por semana", new BigDecimal("24000"), 2, "Hasta 2 actividades por semana");
        Plan tres = new Plan("Tres veces por semana", new BigDecimal("27000"), 3, "Hasta 3 actividades por semana");
        Plan libre = new Plan("Pase libre", new BigDecimal("30000"), 7, "Hasta 7 actividades por semana");
        repositorioPlanes.guardar(List.of(dos, tres, libre));

        // Empleado administrador (DNI 11111111, clave admin123)
        Empleado admin = new Empleado("11111111", "Admin", "Recepción", null, 1);
        admin.setClave(encriptador.encode("admin123"));
        repositorioPersonas.guardar(admin);

        // Actividades de ejemplo
        Actividad funcional = servicioActividad.alta(
                new Actividad("Funcional", DiaSemana.LUNES, LocalTime.of(18, 0), LocalTime.of(19, 0), 15));
        Actividad spinning = servicioActividad.alta(
                new Actividad("Spinning", DiaSemana.MARTES, LocalTime.of(19, 0), LocalTime.of(20, 0), 12));
        servicioActividad.alta(
                new Actividad("Yoga", DiaSemana.MIERCOLES, LocalTime.of(9, 0), LocalTime.of(10, 0), 10));
        servicioActividad.alta(
                new Actividad("Crossfit", DiaSemana.JUEVES, LocalTime.of(20, 0), LocalTime.of(21, 0), 2));
        servicioActividad.alta(
                new Actividad("Zumba", DiaSemana.SABADO, LocalTime.of(10, 0), LocalTime.of(11, 0), 20));

        // Socios de ejemplo (la clave inicial de cada uno es su DNI)
        Socio ana = servicioSocio.alta(nuevoSocio("40111222", "Ana", "Gómez", "ana@mail.com", true, false), dos.getId());
        Socio juan = servicioSocio.alta(nuevoSocio("38222333", "Juan", "Pérez", null, false, true), libre.getId());
        servicioSocio.alta(nuevoSocio("42333444", "Lucía", "Díaz", null, false, false), tres.getId());   // sin pagar

        servicioPago.cobrar(ana.getId(), dos.getId(), MedioPago.EFECTIVO);
        servicioPago.cobrar(juan.getId(), libre.getId(), MedioPago.TRANSFERENCIA);

        servicioActividad.inscribir(funcional.getId(), ana.getId());
        servicioActividad.inscribir(spinning.getId(), juan.getId());

        System.out.println("Datos iniciales cargados. Empleado: DNI 11111111 / clave admin123");
    }

    private Socio nuevoSocio(String dni, String nombre, String apellido, String email,
                             boolean grupoFamiliar, boolean empleadoComercio) {
        Socio s = new Socio();
        s.setDni(dni);
        s.setNombre(nombre);
        s.setApellido(apellido);
        s.setEmail(email);
        s.setGrupoFamiliar(grupoFamiliar);
        s.setEmpleadoComercio(empleadoComercio);
        return s;
    }
}
