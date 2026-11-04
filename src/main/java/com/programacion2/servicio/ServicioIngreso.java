package com.programacion2.servicio;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.programacion2.dominio.Actividad;
import com.programacion2.dominio.DiaSemana;
import com.programacion2.dominio.Ingreso;
import com.programacion2.dominio.Persona;
import com.programacion2.dominio.Socio;
import com.programacion2.excepcion.ServicioException;
import com.programacion2.repositorio.IRepositorio;

@Service
public class ServicioIngreso implements IServicioIngreso {

    private static final int MAXIMO_HISTORIAL = 10;

    private final IRepositorio<Ingreso> repositorio;
    private final IRepositorio<Persona> repositorioPersonas;
    private final IServicioActividad servicioActividad;

    @Autowired
    public ServicioIngreso(IRepositorio<Ingreso> repositorio,
                           IRepositorio<Persona> repositorioPersonas,
                           IServicioActividad servicioActividad) {
        this.repositorio = repositorio;
        this.repositorioPersonas = repositorioPersonas;
        this.servicioActividad = servicioActividad;
    }

    @Override
    public Ingreso registrar(String dni) {
        if (dni == null || dni.isBlank()) {
            throw new ServicioException("Ingresá el DNI.");
        }
        List<Persona> personas = repositorioPersonas.consultar(Persona.class, "porDni", dni.trim());
        if (personas.isEmpty() || !(personas.get(0) instanceof Socio)) {
            throw new ServicioException("No hay ningún socio con el DNI " + dni.trim() + ".");
        }
        Socio socio = (Socio) personas.get(0);
        if (!socio.isActivo()) {
            throw new ServicioException(socio.getNombreCompleto() + " está dado de baja.");
        }
        if (!socio.isAbonoVigente()) {
            String vence = (socio.getVenceAbono() == null) ? "nunca pagó"
                    : "venció el " + socio.getVenceAbono();
            throw new ServicioException("NO PUEDE PASAR: el abono de " + socio.getNombreCompleto()
                    + " no está vigente (" + vence + ").");
        }

        LocalDateTime ahora = LocalDateTime.now();
        Actividad actividad = actividadMasCercana(socio.getId(), ahora);
        Ingreso ingreso = new Ingreso(socio, actividad, ahora);
        repositorio.guardar(ingreso);
        return ingreso;
    }

    @Override
    public List<Ingreso> ingresosDeHoy() {
        // Named query "desde": ingresos desde las 00:00 de hoy
        return repositorio.consultar(Ingreso.class, "desde", LocalDate.now().atStartOfDay());
    }

    @Override
    public List<Ingreso> ingresosDeSocio(int socioId) {
        List<Ingreso> ingresos = repositorio.consultar(Ingreso.class, "porSocio", socioId);
        return ingresos.size() > MAXIMO_HISTORIAL ? ingresos.subList(0, MAXIMO_HISTORIAL) : ingresos;
    }

    /** De las actividades de hoy del socio, la que empieza más cerca de la hora de entrada. */
    private Actividad actividadMasCercana(int socioId, LocalDateTime ahora) {
        DiaSemana hoy = DiaSemana.de(DayOfWeek.from(ahora));
        LocalTime hora = ahora.toLocalTime();
        Actividad elegida = null;
        long menorDiferencia = Long.MAX_VALUE;
        for (Actividad a : servicioActividad.actividadesDeSocio(socioId)) {
            if (a.getDia() == hoy) {
                long diferencia = Math.abs(Duration.between(hora, a.getHoraInicio()).toMinutes());
                if (diferencia < menorDiferencia) {
                    menorDiferencia = diferencia;
                    elegida = a;
                }
            }
        }
        return elegida;
    }
}
