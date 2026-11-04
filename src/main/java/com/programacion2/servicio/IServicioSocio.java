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

    /** Alta con los datos personales: valida, encripta la clave inicial (el DNI) y guarda. */
    Socio alta(Socio datos);

    /** Modificación de los datos personales de un socio. */
    Socio modificar(int id, Socio datos);

    /**
     * Guarda el abono (plan) y los descuentos que se eligieron al cobrar.
     * No deja si ya está anotado en más actividades de las que permite el plan.
     */
    void cambiarAbono(int id, int planId, boolean grupoFamiliar, boolean empleadoComercio);
    /** Baja lógica: queda inactivo y se lo saca de sus actividades. */
    void darDeBaja(int id);

    /** Vuelve a activar un socio dado de baja. */
    void reactivar(int id);

    void cambiarClave(int id, String actual, String nueva, String repetida);

    /** Cantidad de socios activos con el abono vencido o sin pagar. */
    long contarAbonosVencidos();
}
