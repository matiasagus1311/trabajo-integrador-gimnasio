package com.programacion2.servicio;

import java.util.List;

import com.programacion2.dominio.Actividad;
import com.programacion2.dominio.DiaSemana;

public interface IServicioActividad {

    /** Actividades ordenadas por día y horario. dia null = todos los días. */
    List<Actividad> listar(DiaSemana dia);

    /** Solo las activas (las que ve el socio). */
    List<Actividad> listarActivas();

    Actividad consultar(int id);

    Actividad alta(Actividad datos);

    Actividad modificar(int id, Actividad datos);

    /**
     * Elimina la actividad. Si tiene inscriptos, no se puede.
     * Si tiene ingresos registrados, queda desactivada (baja lógica).
     * Devuelve el mensaje para mostrar.
     */
    String eliminar(int id);

    /** Operación de negocio: anota al socio en la actividad, con todas las validaciones. */
    void inscribir(int actividadId, int socioId);

    void desinscribir(int actividadId, int socioId);

    /** Actividades en las que está anotado el socio. */
    List<Actividad> actividadesDeSocio(int socioId);
}
