package com.programacion2.dominio;

import java.time.LocalDateTime;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.Queries;
import javax.jdo.annotations.Query;

/**
 * Registro de entrada de un socio al gimnasio (por DNI).
 * N a 1 con Socio y, si ese día tenía una, con la Actividad a la que venía.
 */
@PersistenceCapable(detachable = "true")
@Queries({
    @Query(name = "desde", language = "JDOQL",
           value = "SELECT FROM com.programacion2.dominio.Ingreso"
                 + " WHERE fechaHora >= :valor ORDER BY fechaHora DESC"),
    @Query(name = "porSocio", language = "JDOQL",
           value = "SELECT FROM com.programacion2.dominio.Ingreso"
                 + " WHERE socio.id == :valor ORDER BY fechaHora DESC"),
    @Query(name = "porActividad", language = "JDOQL",
           value = "SELECT FROM com.programacion2.dominio.Ingreso"
                 + " WHERE actividad.id == :valor")
})
public class Ingreso {

    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private int id;

    @Column(name = "SOCIO_ID")
    @Persistent(defaultFetchGroup = "true")
    private Socio socio;

    @Column(name = "ACTIVIDAD_ID", allowsNull = "true")
    @Persistent(defaultFetchGroup = "true")
    private Actividad actividad;

    private LocalDateTime fechaHora;

    public Ingreso() { }

    public Ingreso(Socio socio, Actividad actividad, LocalDateTime fechaHora) {
        this.socio = socio;
        this.actividad = actividad;
        this.fechaHora = fechaHora;
    }

    public int getId() { return id; }

    public Socio getSocio() { return socio; }
    public void setSocio(Socio socio) { this.socio = socio; }

    public Actividad getActividad() { return actividad; }
    public void setActividad(Actividad actividad) { this.actividad = actividad; }

    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }

    @Override
    public String toString() {
        return "Ingreso [id=" + id + ", " + fechaHora + "]";
    }
}
