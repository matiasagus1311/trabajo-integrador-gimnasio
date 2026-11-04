package com.programacion2.servicio;

import com.programacion2.dominio.MedioPago;
import com.programacion2.dominio.Pago;

public interface IServicioPago {

    /** Arma el pago que se haría hoy (monto y período), SIN guardarlo: para mostrarlo antes de confirmar. */
    Pago prepararCobro(int socioId);

    /**
     * Operación de negocio: cobra el abono elegido (plan y descuentos) y lo extiende un mes.
     * El plan y los descuentos quedan guardados en el socio como su abono actual.
     */
    Pago cobrar(int socioId, int planId, boolean grupoFamiliar, boolean empleadoComercio,
                MedioPago medioPago);
}
