package com.programacion2.servicio;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.programacion2.dominio.MedioPago;
import com.programacion2.dominio.Pago;
import com.programacion2.dominio.Socio;
import com.programacion2.excepcion.ServicioException;
import com.programacion2.repositorio.IRepositorio;

@Service
public class ServicioPago implements IServicioPago {

    private final IRepositorio<Pago> repositorio;
    private final IServicioSocio servicioSocio;
    private final IServicioPlan servicioPlan;

    @Autowired
    public ServicioPago(IRepositorio<Pago> repositorio, IServicioSocio servicioSocio,
                        IServicioPlan servicioPlan) {
        this.repositorio = repositorio;
        this.servicioSocio = servicioSocio;
        this.servicioPlan = servicioPlan;
    }

    @Override
    public Pago prepararCobro(int socioId) {
        Socio socio = servicioSocio.consultar(socioId);
        if (!socio.isActivo()) {
            throw new ServicioException("No se le puede cobrar a un socio dado de baja.");
        }
        LocalDate hoy = LocalDate.now();
        LocalDate vence = socio.getVenceAbono();

        // Si el abono sigue vigente, el mes nuevo arranca al día siguiente del vencimiento.
        // Si está vencido (o nunca pagó), arranca hoy.
        LocalDate desde = (vence != null && !vence.isBefore(hoy)) ? vence.plusDays(1) : hoy;
        LocalDate hasta = desde.plusMonths(1).minusDays(1);

        return new Pago(socio, socio.getPlan(), hoy, socio.calcularCuota(), null, desde, hasta);
    }

    @Override
    public Pago cobrar(int socioId, int planId, boolean grupoFamiliar, boolean empleadoComercio,
                       MedioPago medioPago) {
        if (planId == 0) {
            throw new ServicioException("Elegí el abono que se le cobra.");
        }
        if (medioPago == null) {
            throw new ServicioException("Elegí el medio de pago.");
        }
        if (!servicioSocio.consultar(socioId).isActivo()) {
            throw new ServicioException("No se le puede cobrar a un socio dado de baja.");
        }
        servicioPlan.consultar(planId);                  // valida que el plan exista
        // Se guarda el abono elegido en el socio (o avisa por qué no se puede)
        servicioSocio.cambiarAbono(socioId, planId, grupoFamiliar, empleadoComercio);
        Pago pago = prepararCobro(socioId);              // monto con el abono ya actualizado
        pago.setMedioPago(medioPago);
        repositorio.guardar(pago);        // INSERT en pago con SOCIO_ID
        return pago;
    }
}
