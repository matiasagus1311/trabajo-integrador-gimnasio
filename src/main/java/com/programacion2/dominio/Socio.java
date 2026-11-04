package com.programacion2.dominio;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

/**
 * Socio del gimnasio.
 * - N a 1 con Plan (columna PLAN_ID en la tabla socio).
 * - 1 a N con Pago (bidireccional: la FK SOCIO_ID está en pago).
 * - Las actividades en las que está anotado se consultan desde Actividad
 *   (Actividad es la dueña de la relación M a N).
 */
@PersistenceCapable(detachable = "true")
@Inheritance(strategy = InheritanceStrategy.NEW_TABLE)
public class Socio extends Persona {

    // Descuentos del TP del 1er cuatrimestre
    public static final BigDecimal DESCUENTO_GRUPO_FAMILIAR = new BigDecimal("0.10");
    public static final BigDecimal DESCUENTO_EMPLEADO_COMERCIO = new BigDecimal("0.20");

    private LocalDate fechaAlta;

    private boolean grupoFamiliar;

    private boolean empleadoComercio;

    private boolean activo = true;

    @Column(name = "PLAN_ID")
    @Persistent(defaultFetchGroup = "true")
    private Plan plan;

    @Persistent(mappedBy = "socio", defaultFetchGroup = "true")
    private List<Pago> pagos = new ArrayList<>();

    public Socio() { }

    public Socio(String dni, String nombre, String apellido, String email, Plan plan) {
        super(dni, nombre, apellido, email);
        this.plan = plan;
        this.fechaAlta = LocalDate.now();
    }

    public LocalDate getFechaAlta() { return fechaAlta; }
    public void setFechaAlta(LocalDate fechaAlta) { this.fechaAlta = fechaAlta; }

    public boolean isGrupoFamiliar() { return grupoFamiliar; }
    public void setGrupoFamiliar(boolean grupoFamiliar) { this.grupoFamiliar = grupoFamiliar; }

    public boolean isEmpleadoComercio() { return empleadoComercio; }
    public void setEmpleadoComercio(boolean empleadoComercio) { this.empleadoComercio = empleadoComercio; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public Plan getPlan() { return plan; }
    public void setPlan(Plan plan) { this.plan = plan; }

    public List<Pago> getPagos() { return pagos; }

    /**
     * Cuota mensual: precio del plan, -10% si es grupo familiar
     * y -20% sobre lo que queda si es empleado de comercio.
     */
    public BigDecimal calcularCuota() {
        return calcularCuota(plan);
    }

    /** Cuota que pagaría este socio con otro plan (se usa al cobrar, para elegir el abono). */
    public BigDecimal calcularCuota(Plan otroPlan) {
        if (otroPlan == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal cuota = otroPlan.getPrecioMensual();
        if (grupoFamiliar) {
            cuota = cuota.subtract(cuota.multiply(DESCUENTO_GRUPO_FAMILIAR));
        }
        if (empleadoComercio) {
            cuota = cuota.subtract(cuota.multiply(DESCUENTO_EMPLEADO_COMERCIO));
        }
        return cuota.setScale(2, RoundingMode.HALF_UP);
    }

    /** Último día cubierto por los pagos, o null si nunca pagó. */
    public LocalDate getVenceAbono() {
        LocalDate vence = null;
        for (Pago p : pagos) {
            if (vence == null || p.getHasta().isAfter(vence)) {
                vence = p.getHasta();
            }
        }
        return vence;
    }

    /** El abono está vigente si algún pago cubre la fecha indicada. */
    public boolean tieneAbonoVigente(LocalDate fecha) {
        for (Pago p : pagos) {
            if (p.cubre(fecha)) {
                return true;
            }
        }
        return false;
    }

    public boolean isAbonoVigente() {
        return tieneAbonoVigente(LocalDate.now());
    }

    /** Pagos del más nuevo al más viejo, para mostrarlos. */
    public List<Pago> getPagosOrdenados() {
        List<Pago> ordenados = new ArrayList<>(pagos);
        ordenados.sort((a, b) -> b.getFecha().compareTo(a.getFecha()) != 0
                ? b.getFecha().compareTo(a.getFecha())
                : Integer.compare(b.getId(), a.getId()));
        return ordenados;
    }

    public Pago getUltimoPago() {
        List<Pago> ordenados = getPagosOrdenados();
        return ordenados.isEmpty() ? null : ordenados.get(0);
    }
}
