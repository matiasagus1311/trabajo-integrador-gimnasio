package com.programacion2.servicio;

import java.util.List;

import com.programacion2.dominio.Plan;

public interface IServicioPlan {

    /** Todos los planes, del más barato al más caro. */
    List<Plan> listar();

    /** El plan con ese id (con sus socios). Si no existe, ServicioException. */
    Plan consultar(int id);
}
