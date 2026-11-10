package com.programacion2.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada de la aplicación web.
 * scanBasePackages: Spring busca @Configuration, @Service, @Controller y @Component
 * en todos los paquetes de com.programacion2 (no solo en "app").
 */
@SpringBootApplication(scanBasePackages = "com.programacion2")
public class Aplicacion {

    public static void main(String[] args) {
        SpringApplication.run(Aplicacion.class, args);
        // Cuando llega acá la página ya está andando
        System.out.println("Página lista: http://localhost:8080");
    }
}
