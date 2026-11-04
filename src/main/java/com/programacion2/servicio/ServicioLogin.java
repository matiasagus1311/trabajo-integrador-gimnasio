package com.programacion2.servicio;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.programacion2.dominio.Empleado;
import com.programacion2.dominio.Persona;
import com.programacion2.dominio.Socio;
import com.programacion2.excepcion.ServicioException;
import com.programacion2.repositorio.IRepositorio;

@Service
public class ServicioLogin implements IServicioLogin {

    private final IRepositorio<Persona> repositorioPersonas;
    private final PasswordEncoder encriptador;

    @Autowired
    public ServicioLogin(IRepositorio<Persona> repositorioPersonas, PasswordEncoder encriptador) {
        this.repositorioPersonas = repositorioPersonas;
        this.encriptador = encriptador;
    }

    @Override
    public UsuarioSesion autenticar(String dni, String clave) {
        if (dni == null || dni.isBlank() || clave == null || clave.isBlank()) {
            throw new ServicioException("Completá el DNI y la clave.");
        }
        List<Persona> personas = repositorioPersonas.consultar(Persona.class, "porDni", dni.trim());
        // Mismo mensaje si no existe o si la clave está mal: no damos pistas
        if (personas.isEmpty() || personas.get(0).getClave() == null
                || !encriptador.matches(clave, personas.get(0).getClave())) {
            throw new ServicioException("DNI o clave incorrectos.");
        }
        Persona persona = personas.get(0);

        if (persona instanceof Empleado) {
            return new UsuarioSesion(persona.getId(), persona.getNombreCompleto(), UsuarioSesion.Rol.EMPLEADO);
        }
        Socio socio = (Socio) persona;
        if (!socio.isActivo()) {
            throw new ServicioException("Tu usuario está dado de baja. Acercate a la recepción.");
        }
        return new UsuarioSesion(socio.getId(), socio.getNombreCompleto(), UsuarioSesion.Rol.SOCIO);
    }
}
