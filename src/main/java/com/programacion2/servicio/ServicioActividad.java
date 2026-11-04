package com.programacion2.servicio;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.programacion2.dominio.Actividad;
import com.programacion2.dominio.DiaSemana;
import com.programacion2.dominio.Ingreso;
import com.programacion2.dominio.Socio;
import com.programacion2.excepcion.ServicioException;
import com.programacion2.repositorio.IRepositorio;

@Service
public class ServicioActividad implements IServicioActividad {

    private static final Comparator<Actividad> POR_DIA_Y_HORA =
            Comparator.comparing(Actividad::getDia).thenComparing(Actividad::getHoraInicio);

    private final IRepositorio<Actividad> repositorio;
    private final IRepositorio<Ingreso> repositorioIngresos;
    private final IServicioSocio servicioSocio;

    @Autowired
    public ServicioActividad(IRepositorio<Actividad> repositorio,
                             IRepositorio<Ingreso> repositorioIngresos,
                             IServicioSocio servicioSocio) {
        this.repositorio = repositorio;
        this.repositorioIngresos = repositorioIngresos;
        this.servicioSocio = servicioSocio;
    }

    @Override
    public List<Actividad> listar(DiaSemana dia) {
        List<Actividad> resultado = new ArrayList<>();
        for (Actividad a : repositorio.listar(Actividad.class)) {
            if (dia == null || a.getDia() == dia) {
                resultado.add(a);
            }
        }
        resultado.sort(POR_DIA_Y_HORA);
        return resultado;
    }

    @Override
    public List<Actividad> listarActivas() {
        List<Actividad> activas = new ArrayList<>();
        for (Actividad a : listar(null)) {
            if (a.isActiva()) {
                activas.add(a);
            }
        }
        return activas;
    }

    @Override
    public Actividad consultar(int id) {
        Actividad actividad = repositorio.consultar(Actividad.class, id);
        if (actividad == null) {
            throw new ServicioException("No existe la actividad.");
        }
        return actividad;
    }

    @Override
    public Actividad alta(Actividad datos) {
        validarHorario(datos);
        Actividad nueva = new Actividad(datos.getNombre().trim(), datos.getDia(),
                datos.getHoraInicio(), datos.getHoraFin(), datos.getCupo());
        nueva.setActiva(true);
        repositorio.guardar(nueva);
        return nueva;
    }

    @Override
    public Actividad modificar(int id, Actividad datos) {
        Actividad actividad = consultar(id);
        validarHorario(datos);
        if (datos.getCupo() < actividad.getInscriptos().size()) {
            throw new ServicioException("El cupo no puede ser menor que los "
                    + actividad.getInscriptos().size() + " socios ya inscriptos.");
        }
        actividad.setNombre(datos.getNombre().trim());
        actividad.setDia(datos.getDia());
        actividad.setHoraInicio(datos.getHoraInicio());
        actividad.setHoraFin(datos.getHoraFin());
        actividad.setCupo(datos.getCupo());
        actividad.setActiva(datos.isActiva());
        repositorio.guardar(actividad);
        return actividad;
    }

    @Override
    public String eliminar(int id) {
        Actividad actividad = consultar(id);
        if (!actividad.getInscriptos().isEmpty()) {
            throw new ServicioException("No se puede eliminar \"" + actividad.getNombre()
                    + "\": tiene " + actividad.getInscriptos().size()
                    + " socios inscriptos. Primero sacalos de la actividad.");
        }
        List<Ingreso> ingresos = repositorioIngresos.consultar(Ingreso.class, "porActividad", id);
        if (!ingresos.isEmpty()) {
            // Tiene historial: se desactiva para no dejar ingresos apuntando a nada
            actividad.setActiva(false);
            repositorio.guardar(actividad);
            return "La actividad tenía ingresos registrados: quedó desactivada.";
        }
        repositorio.eliminar(Actividad.class, id);
        return "Actividad eliminada.";
    }

    @Override
    public void inscribir(int actividadId, int socioId) {
        Actividad actividad = consultar(actividadId);
        Socio socio = servicioSocio.consultar(socioId);

        if (!socio.isActivo()) {
            throw new ServicioException("El socio está dado de baja.");
        }
        if (!socio.isAbonoVigente()) {
            throw new ServicioException("El abono de " + socio.getNombreCompleto()
                    + " no está vigente. Primero tiene que pagar la cuota.");
        }
        if (!actividad.isActiva()) {
            throw new ServicioException("La actividad no está activa.");
        }
        if (actividad.estaInscripto(socio)) {
            throw new ServicioException(socio.getNombreCompleto() + " ya está anotado en "
                    + actividad.getNombre() + ".");
        }
        if (!actividad.hayCupo()) {
            throw new ServicioException("La actividad " + actividad.getNombre() + " no tiene cupo.");
        }

        List<Actividad> actuales = actividadesDeSocio(socioId);
        int maximo = socio.getPlan().getMaxActividades();
        if (actuales.size() >= maximo) {
            throw new ServicioException("El plan " + socio.getPlan().getNombre()
                    + " permite hasta " + maximo + " actividades y ya tiene " + actuales.size() + ".");
        }
        for (Actividad otra : actuales) {
            if (otra.seSuperponeCon(actividad)) {
                throw new ServicioException("Se superpone con " + otra.getNombre()
                        + " (" + otra.getHorario() + ").");
            }
        }

        actividad.agregarInscripto(socio);
        repositorio.guardar(actividad);   // agrega la fila en ACTIVIDAD_SOCIOS
    }

    @Override
    public void desinscribir(int actividadId, int socioId) {
        Actividad actividad = consultar(actividadId);
        Socio socio = servicioSocio.consultar(socioId);
        if (!actividad.estaInscripto(socio)) {
            throw new ServicioException(socio.getNombreCompleto() + " no está anotado en "
                    + actividad.getNombre() + ".");
        }
        actividad.quitarInscripto(socio);
        repositorio.guardar(actividad);   // borra la fila de ACTIVIDAD_SOCIOS
    }

    @Override
    public List<Actividad> actividadesDeSocio(int socioId) {
        // Named query "porSocio" de Actividad
        List<Actividad> actividades = repositorio.consultar(Actividad.class, "porSocio", socioId);
        actividades.sort(POR_DIA_Y_HORA);
        return actividades;
    }

    private void validarHorario(Actividad datos) {
        if (datos.getHoraInicio() == null || datos.getHoraFin() == null) {
            throw new ServicioException("Completá el horario.");
        }
        if (!datos.getHoraFin().isAfter(datos.getHoraInicio())) {
            throw new ServicioException("La hora de fin tiene que ser posterior a la de inicio.");
        }
    }
}
