package com.programacion2.dominio;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.Element;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.Join;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.Query;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Actividad (clase) del gimnasio, con día, horario y cupo.
 * Relación M a N con Socio: Actividad es la DUEÑA y arma la
 * tabla intermedia ACTIVIDAD_SOCIOS (ACTIVIDAD_ID, SOCIO_ID).
 */
@PersistenceCapable(detachable = "true")
@Query(name = "porSocio", language = "JDOQL",
       value = "SELECT FROM com.programacion2.dominio.Actividad"
             + " WHERE inscriptos.contains(s) && s.id == :valor"
             + " VARIABLES com.programacion2.dominio.Socio s")
public class Actividad {

    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private int id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 50, message = "El nombre puede tener hasta 50 letras")
    private String nombre;

    @NotNull(message = "Elegí el día")
    @Column(jdbcType = "VARCHAR", length = 15)
    private DiaSemana dia;

    @NotNull(message = "Poné la hora de inicio")
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime horaInicio;

    @NotNull(message = "Poné la hora de fin")
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime horaFin;

    @Min(value = 1, message = "El cupo tiene que ser al menos 1")
    @Max(value = 100, message = "El cupo puede ser hasta 100")
    private int cupo;

    private boolean activa = true;

    @Persistent(table = "ACTIVIDAD_SOCIOS", defaultFetchGroup = "true")
    @Join(column = "ACTIVIDAD_ID")       // columna que apunta a la actividad (dueña)
    @Element(column = "SOCIO_ID")        // columna que apunta a cada socio
    private List<Socio> inscriptos = new ArrayList<>();

    public Actividad() { }

    public Actividad(String nombre, DiaSemana dia, LocalTime horaInicio, LocalTime horaFin, int cupo) {
        this.nombre = nombre;
        this.dia = dia;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.cupo = cupo;
    }

    public int getId() { return id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public DiaSemana getDia() { return dia; }
    public void setDia(DiaSemana dia) { this.dia = dia; }

    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }

    public LocalTime getHoraFin() { return horaFin; }
    public void setHoraFin(LocalTime horaFin) { this.horaFin = horaFin; }

    public int getCupo() { return cupo; }
    public void setCupo(int cupo) { this.cupo = cupo; }

    public boolean isActiva() { return activa; }
    public void setActiva(boolean activa) { this.activa = activa; }

    public List<Socio> getInscriptos() { return inscriptos; }

    public int getLugaresLibres() {
        return cupo - inscriptos.size();
    }

    public boolean hayCupo() {
        return getLugaresLibres() > 0;
    }

    public boolean estaInscripto(Socio socio) {
        return inscriptos.contains(socio);   // usa equals por id
    }

    public void agregarInscripto(Socio socio) {
        if (!inscriptos.contains(socio)) {   // no anotarlo dos veces
            inscriptos.add(socio);
        }
    }

    public void quitarInscripto(Socio socio) {
        inscriptos.remove(socio);
    }

    /** Dos actividades se superponen si son el mismo día y sus horarios se pisan. */
    public boolean seSuperponeCon(Actividad otra) {
        return dia == otra.dia
                && horaInicio.isBefore(otra.horaFin)
                && otra.horaInicio.isBefore(horaFin);
    }

    public String getHorario() {
        return dia.getTexto() + " " + horaInicio + " a " + horaFin;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Actividad)) return false;
        Actividad otra = (Actividad) o;
        return id != 0 && id == otra.id;
    }

    @Override
    public int hashCode() { return Integer.hashCode(id); }

    @Override
    public String toString() {
        return "Actividad [id=" + id + ", " + nombre + ", " + dia + " " + horaInicio + "]";
    }
}
