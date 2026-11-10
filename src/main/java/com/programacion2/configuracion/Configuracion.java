package com.programacion2.configuracion;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManagerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.programacion2.dominio.Actividad;
import com.programacion2.dominio.Ingreso;
import com.programacion2.dominio.Pago;
import com.programacion2.dominio.Persona;
import com.programacion2.dominio.Plan;
import com.programacion2.dominio.Socio;
import com.programacion2.repositorio.IRepositorio;
import com.programacion2.repositorio.Repositorio;

/**
 * Configuración de Spring con anotaciones (como en la Unidad 5 y 6):
 * acá se definen los beans que Spring después inyecta con @Autowired.
 */
@Configuration
public class Configuracion implements WebMvcConfigurer {

    private final ControlSesion controlSesion;

    @Autowired
    public Configuracion(ControlSesion controlSesion) {
        this.controlSesion = controlSesion;
    }

    /**
     * La fábrica de PersistenceManager: se crea UNA sola vez con la unidad
     * "Programacion2" del persistence.xml (ahí están la URL, el usuario
     * y la contraseña de la base, como en clase).
     * Spring la cierra al apagar la aplicación (destroyMethod).
     */
    @Bean(destroyMethod = "close")
    public PersistenceManagerFactory persistenceManagerFactory() {
        return JDOHelper.getPersistenceManagerFactory("Programacion2");
    }

    // Un repositorio por clase del dominio, todos con la misma fábrica

    @Bean
    public IRepositorio<Persona> repositorioPersonas(PersistenceManagerFactory pmf) {
        return new Repositorio<>(pmf);
    }

    @Bean
    public IRepositorio<Socio> repositorioSocios(PersistenceManagerFactory pmf) {
        return new Repositorio<>(pmf);
    }

    @Bean
    public IRepositorio<Plan> repositorioPlanes(PersistenceManagerFactory pmf) {
        return new Repositorio<>(pmf);
    }

    @Bean
    public IRepositorio<Actividad> repositorioActividades(PersistenceManagerFactory pmf) {
        return new Repositorio<>(pmf);
    }

    @Bean
    public IRepositorio<Pago> repositorioPagos(PersistenceManagerFactory pmf) {
        return new Repositorio<>(pmf);
    }

    @Bean
    public IRepositorio<Ingreso> repositorioIngresos(PersistenceManagerFactory pmf) {
        return new Repositorio<>(pmf);
    }

    /** Encripta y compara claves con BCrypt. */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /** Antes de entrar a /empleado/** o /socio/** se controla quién inició sesión. */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(controlSesion)
                .addPathPatterns("/empleado/**", "/socio/**");
    }
}
