package com.programacion2.servicio;

import java.util.List;

import com.programacion2.dominio.Ingreso;

public interface IServicioIngreso {

    /**
     * Operación de negocio: registra la entrada del socio con ese DNI.
     * Si no puede pasar (no existe, dado de baja, abono vencido), ServicioException con el motivo.
     */
    Ingreso registrar(String dni);

    /** Ingresos de hoy, del más reciente al más viejo. */
    List<Ingreso> ingresosDeHoy();

    /** Últimos ingresos de un socio. */
    List<Ingreso> ingresosDeSocio(int socioId);
}
