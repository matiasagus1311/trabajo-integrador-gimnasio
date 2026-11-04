package com.programacion2.dominio;

import java.math.BigDecimal;
import java.time.LocalDate;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * Pago de la cuota. Cubre un período (desde - hasta): eso define
 * hasta cuándo está vigente el abono del socio.
 * N a 1 con Socio: columna SOCIO_ID en la tabla pago.
 */
@PersistenceCapable(detachable = "true")
public class Pago {

    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private int id;

    @Column(name = "SOCIO_ID")
    @Persistent(defaultFetchGroup = "true")
    private Socio socio;

    // Abono que se cobró en este pago (N a 1 con Plan)
    @Column(name = "PLAN_ID")
    @Persistent(defaultFetchGroup = "true")
    private Plan plan;

    private LocalDate fecha;

    @Column(length = 12, scale = 2)
    private BigDecimal monto;

    @Column(jdbcType = "VARCHAR", length = 20)
    private MedioPago medioPago;

    private LocalDate desde;

    private LocalDate hasta;

    public Pago() { }

    public Pago(Socio socio, Plan plan, LocalDate fecha, BigDecimal monto, MedioPago medioPago,
                LocalDate desde, LocalDate hasta) {
        this.socio = socio;
        this.plan = plan;
        this.fecha = fecha;
        this.monto = monto;
        this.medioPago = medioPago;
        this.desde = desde;
        this.hasta = hasta;
    }

    public int getId() { return id; }

    public Socio getSocio() { return socio; }
    public void setSocio(Socio socio) { this.socio = socio; }

    public Plan getPlan() { return plan; }
    public void setPlan(Plan plan) { this.plan = plan; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }

    public MedioPago getMedioPago() { return medioPago; }
    public void setMedioPago(MedioPago medioPago) { this.medioPago = medioPago; }

    public LocalDate getDesde() { return desde; }
    public void setDesde(LocalDate desde) { this.desde = desde; }

    public LocalDate getHasta() { return hasta; }
    public void setHasta(LocalDate hasta) { this.hasta = hasta; }

    /** ¿Este pago cubre esa fecha? */
    public boolean cubre(LocalDate dia) {
        return !dia.isBefore(desde) && !dia.isAfter(hasta);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pago)) return false;
        Pago otro = (Pago) o;
        return id != 0 && id == otro.id;
    }

    @Override
    public int hashCode() { return Integer.hashCode(id); }

    @Override
    public String toString() {
        return "Pago [id=" + id + ", $" + monto + ", " + desde + " a " + hasta + "]";
    }
}
