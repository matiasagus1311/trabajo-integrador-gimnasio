package com.programacion2.servicio;

import java.util.List;

import com.programacion2.dominio.Socio;

public interface IServicioSocio {

    /**
     * Socios filtrados. texto: nombre, apellido o DNI (vacío = todos).
     * planId: 0 = todos. estado: "" (activos), "vigente", "vencido", "baja" o "todos".
     */
    List<Socio> buscar(String texto, int planId, String estado);

    /** Socios activos con el abono al día (los que se pueden anotar a actividades). */
    List<Socio> listarHabilitados();

    Socio consultar(int id);

    /** Alta: valida, encripta la clave inicial (el DNI) y guarda. */
    Socio alta(Socio datos, int planId);

    /** Modificación de los datos y el plan de un socio. */
    Socio modificar(int id, Socio datos, int planId);

    /** Cambia el abono (plan) del socio. No deja si ya está anotado en más actividades de las que permite el plan nuevo. */
    void cambiarPlan(int id, int planId);
    /** Baja lógica: queda inactivo y se lo saca de sus actividades. */
    void darDeBaja(int id);

    /** Vuelve a activar un socio dado de baja. */
    void reactivar(int id);

    void cambiarClave(int id, String actual, String nueva, String repetida);

    /** Cantidad de socios activos con el abono vencido o sin pagar. */
    long contarAbonosVencidos();
}
