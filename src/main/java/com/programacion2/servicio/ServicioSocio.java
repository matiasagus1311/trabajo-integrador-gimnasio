package com.programacion2.servicio;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.programacion2.dominio.Actividad;
import com.programacion2.dominio.Persona;
import com.programacion2.dominio.Plan;
import com.programacion2.dominio.Socio;
import com.programacion2.excepcion.ServicioException;
import com.programacion2.repositorio.IRepositorio;

@Service
public class ServicioSocio implements IServicioSocio {

    private final IRepositorio<Socio> repositorio;
    private final IRepositorio<Persona> repositorioPersonas;
    private final IRepositorio<Actividad> repositorioActividades;
    private final IServicioPlan servicioPlan;
    private final PasswordEncoder encriptador;

    @Autowired
    public ServicioSocio(IRepositorio<Socio> repositorio,
                         IRepositorio<Persona> repositorioPersonas,
                         IRepositorio<Actividad> repositorioActividades,
                         IServicioPlan servicioPlan,
                         PasswordEncoder encriptador) {
        this.repositorio = repositorio;
        this.repositorioPersonas = repositorioPersonas;
        this.repositorioActividades = repositorioActividades;
        this.servicioPlan = servicioPlan;
        this.encriptador = encriptador;
    }

    @Override
    public List<Socio> buscar(String texto, int planId, String estado) {
        List<Socio> socios = repositorio.listar(Socio.class);
        String buscado = normalizar(texto);

        String filtro = (estado == null) ? "" : estado;
        List<Socio> resultado = new ArrayList<>();
        for (Socio s : socios) {
            // Texto: busca en nombre, apellido o DNI, sin importar mayúsculas ni tildes ("gom" encuentra "Gómez")
            if (!buscado.isEmpty()
                    && !normalizar(s.getNombre()).contains(buscado)
                    && !normalizar(s.getApellido()).contains(buscado)
                    && !s.getDni().contains(buscado)) {
                continue;
            }
            if (planId > 0 && (s.getPlan() == null || s.getPlan().getId() != planId)) {
                continue;
            }
            boolean pasa = switch (filtro) {
                case "todos" -> true;
                case "baja" -> !s.isActivo();
                case "vigente" -> s.isActivo() && s.isAbonoVigente();
                case "vencido" -> s.isActivo() && !s.isAbonoVigente();
                default -> s.isActivo();
            };
            if (pasa) {
                resultado.add(s);
            }
        }
        resultado.sort(Comparator.comparing(Socio::getApellido, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(Socio::getNombre, String.CASE_INSENSITIVE_ORDER));
        return resultado;
    }

    /**
     * Cada palabra con la primera letra en mayúscula y el resto en minúscula:
     * "mAtias ibañeZ" -> "Matias Ibañez", "juan  pablo" -> "Juan Pablo".
     */
    private static String capitalizar(String texto) {
        StringBuilder resultado = new StringBuilder();
        for (String palabra : texto.trim().toLowerCase().split("\\s+")) {
            if (palabra.isEmpty()) {
                continue;
            }
            if (resultado.length() > 0) {
                resultado.append(' ');
            }
            resultado.append(Character.toUpperCase(palabra.charAt(0))).append(palabra.substring(1));
        }
        return resultado.toString();
    }

    /** Pasa a minúsculas y saca las tildes: "Gómez" -> "gomez". */
    private static String normalizar(String texto) {
        if (texto == null) {
            return "";
        }
        String sinTildes = Normalizer.normalize(texto.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return sinTildes.toLowerCase();
    }

    @Override
    public List<Socio> listarHabilitados() {
        return buscar("", 0, "vigente");
    }

    @Override
    public Socio consultar(int id) {
        Socio socio = repositorio.consultar(Socio.class, id);
        if (socio == null) {
            throw new ServicioException("No existe el socio.");
        }
        return socio;
    }

    @Override
    public Socio alta(Socio datos, int planId) {
        validarDniLibre(datos.getDni(), 0);
        Plan plan = servicioPlan.consultar(planId);

        Socio socio = new Socio(datos.getDni().trim(), capitalizar(datos.getNombre()),
                capitalizar(datos.getApellido()), limpiar(datos.getEmail()), plan);
        socio.setGrupoFamiliar(datos.isGrupoFamiliar());
        socio.setEmpleadoComercio(datos.isEmpleadoComercio());
        socio.setActivo(true);
        // La clave inicial es el DNI; el socio la cambia desde "Mi cuenta"
        socio.setClave(encriptador.encode(socio.getDni()));

        repositorio.guardar(socio);
        return socio;
    }

    @Override
    public Socio modificar(int id, Socio datos, int planId) {
        Socio socio = consultar(id);                 // queda desatado (detached)
        validarDniLibre(datos.getDni(), id);
        Plan plan = servicioPlan.consultar(planId);

        socio.setDni(datos.getDni().trim());
        socio.setNombre(capitalizar(datos.getNombre()));
        socio.setApellido(capitalizar(datos.getApellido()));
        socio.setEmail(limpiar(datos.getEmail()));
        socio.setGrupoFamiliar(datos.isGrupoFamiliar());
        socio.setEmpleadoComercio(datos.isEmpleadoComercio());
        socio.setPlan(plan);

        repositorio.guardar(socio);                  // detached-dirty -> UPDATE
        return socio;
    }

    @Override
    public void cambiarPlan(int id, int planId) {
        Socio socio = consultar(id);
        Plan plan = servicioPlan.consultar(planId);
        if (socio.getPlan() != null && socio.getPlan().getId() == plan.getId()) {
            return;                                  // es el mismo plan: no hay nada que cambiar
        }
        int anotadas = repositorioActividades.consultar(Actividad.class, "porSocio", id).size();
        if (anotadas > plan.getMaxActividades()) {
            throw new ServicioException("Con el plan " + plan.getNombre() + " puede tener hasta "
                    + plan.getMaxActividades() + " actividades y está anotado en " + anotadas
                    + ". Primero sacalo de alguna.");
        }
        socio.setPlan(plan);
        repositorio.guardar(socio);                  // UPDATE de PLAN_ID
    }

    @Override
    public void darDeBaja(int id) {
        Socio socio = consultar(id);
        if (!socio.isActivo()) {
            throw new ServicioException("El socio ya estaba dado de baja.");
        }
        // 1) Se lo saca de todas sus actividades (todas juntas, en una transacción)
        List<Actividad> actividades =
                repositorioActividades.consultar(Actividad.class, "porSocio", id);
        for (Actividad a : actividades) {
            a.quitarInscripto(socio);
        }
        if (!actividades.isEmpty()) {
            repositorioActividades.guardar(actividades);
        }
        // 2) Baja lógica: los pagos e ingresos quedan como historial
        socio.setActivo(false);
        repositorio.guardar(socio);
    }

    @Override
    public void reactivar(int id) {
        Socio socio = consultar(id);
        if (socio.isActivo()) {
            throw new ServicioException("El socio ya está activo.");
        }
        socio.setActivo(true);
        repositorio.guardar(socio);
    }

    @Override
    public void cambiarClave(int id, String actual, String nueva, String repetida) {
        Socio socio = consultar(id);
        if (actual == null || !encriptador.matches(actual, socio.getClave())) {
            throw new ServicioException("La clave actual no es correcta.");
        }
        if (nueva == null || nueva.length() < 6) {
            throw new ServicioException("La clave nueva tiene que tener al menos 6 caracteres.");
        }
        if (!nueva.equals(repetida)) {
            throw new ServicioException("Las dos claves nuevas no coinciden.");
        }
        socio.setClave(encriptador.encode(nueva));
        repositorio.guardar(socio);
    }

    @Override
    public long contarAbonosVencidos() {
        return buscar("", 0, "vencido").size();
    }

    /** El DNI no puede estar usado por otra persona (socio o empleado). */
    private void validarDniLibre(String dni, int idPropio) {
        if (dni == null || dni.isBlank()) {
            throw new ServicioException("El DNI es obligatorio.");
        }
        List<Persona> conEseDni = repositorioPersonas.consultar(Persona.class, "porDni", dni.trim());
        for (Persona p : conEseDni) {
            if (p.getId() != idPropio) {
                throw new ServicioException("Ya hay una persona registrada con el DNI " + dni + ".");
            }
        }
    }

    private String limpiar(String texto) {
        return (texto == null || texto.isBlank()) ? null : texto.trim();
    }
}
