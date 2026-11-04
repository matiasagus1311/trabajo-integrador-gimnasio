package com.programacion2.servicio;

import com.programacion2.dominio.MedioPago;
import com.programacion2.dominio.Pago;

public interface IServicioPago {

    /** Arma el pago que se haría hoy (monto y período), SIN guardarlo: para mostrarlo antes de confirmar. */
    Pago prepararCobro(int socioId);

    /** Operación de negocio: cobra la cuota y extiende el abono un mes. */
    Pago cobrar(int socioId, MedioPago medioPago);
}
