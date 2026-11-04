package com.programacion2.dominio;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.Query;
import javax.jdo.annotations.Unique;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Lo común de las personas del sistema: socios y empleados.
 * Es abstracta: no se crean "personas" sueltas.
 * Herencia NEW_TABLE: tabla persona + una tabla por subclase, unidas por el id.
 */
@PersistenceCapable(detachable = "true")
@Inheritance(strategy = InheritanceStrategy.NEW_TABLE)
@Query(name = "porDni", language = "JDOQL",
       value = "SELECT FROM com.programacion2.dominio.Persona WHERE dni == :valor")
public abstract class Persona {

    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private int id;

    @Unique                                   // no puede haber dos personas con el mismo DNI
    @NotBlank(message = "El DNI es obligatorio")
    @Pattern(regexp = "\\d{7,8}", message = "El DNI tiene que tener 7 u 8 números, sin puntos")
    private String dni;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 50, message = "El nombre puede tener hasta 50 letras")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(max = 50, message = "El apellido puede tener hasta 50 letras")
    private String apellido;

    @Email(message = "El email no es válido")
    @Size(max = 100)
    private String email;

    // Clave encriptada con BCrypt (nunca se guarda la clave real)
    private String clave;

    public Persona() { }

    public Persona(String dni, String nombre, String apellido, String email) {
        this.dni = dni;
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
    }

    public int getId() { return id; }

    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getClave() { return clave; }
    public void setClave(String clave) { this.clave = clave; }

    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }

    /** Dos personas son la misma si tienen el mismo id (sirve para contains en las listas). */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Persona otra = (Persona) o;
        return id != 0 && id == otra.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [id=" + id + ", dni=" + dni
                + ", " + nombre + " " + apellido + "]";
    }
}
