package com.programacion2.dominio;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * Plan (pase) del gimnasio: define el precio mensual y
 * a cuántas actividades se puede anotar el socio.
 * Relación 1 a N bidireccional con Socio: la FK (PLAN_ID) está en socio.
 */
@PersistenceCapable(detachable = "true")
public class Plan {

    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private int id;

    private String nombre;

    @Column(length = 12, scale = 2)
    private BigDecimal precioMensual;

    private int maxActividades;

    private String descripcion;

    // Lado "uno" de la relación: lo maneja el atributo "plan" de Socio
    @Persistent(mappedBy = "plan", defaultFetchGroup = "true")
    private List<Socio> socios = new ArrayList<>();

    public Plan() { }

    public Plan(String nombre, BigDecimal precioMensual, int maxActividades, String descripcion) {
        this.nombre = nombre;
        this.precioMensual = precioMensual;
        this.maxActividades = maxActividades;
        this.descripcion = descripcion;
    }

    public int getId() { return id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public BigDecimal getPrecioMensual() { return precioMensual; }
    public void setPrecioMensual(BigDecimal precioMensual) { this.precioMensual = precioMensual; }

    public int getMaxActividades() { return maxActividades; }
    public void setMaxActividades(int maxActividades) { this.maxActividades = maxActividades; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public List<Socio> getSocios() { return socios; }

    /** Socios activos del plan (los dados de baja no cuentan). */
    public List<Socio> getSociosActivos() {
        List<Socio> activos = new ArrayList<>();
        for (Socio s : socios) {
            if (s.isActivo()) {
                activos.add(s);
            }
        }
        return activos;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Plan)) return false;
        Plan otro = (Plan) o;
        return id != 0 && id == otro.id;
    }

    @Override
    public int hashCode() { return Integer.hashCode(id); }

    @Override
    public String toString() {
        return "Plan [id=" + id + ", " + nombre + ", $" + precioMensual + "]";  // sin la lista: evita ciclos
    }
}
