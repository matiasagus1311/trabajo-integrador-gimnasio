package com.programacion2.servicio;

import com.programacion2.dominio.MedioPago;
import com.programacion2.dominio.Pago;

public interface IServicioPago {

    /** Arma el pago que se haría hoy (monto y período), SIN guardarlo: para mostrarlo antes de confirmar. */
    Pago prepararCobro(int socioId);

    /**
     * Operación de negocio: cobra el abono elegido (planId) y lo extiende un mes.
     * Si el plan elegido es distinto del que tenía, se le cambia el plan al socio.
     */
    Pago cobrar(int socioId, int planId, MedioPago medioPago);
}
