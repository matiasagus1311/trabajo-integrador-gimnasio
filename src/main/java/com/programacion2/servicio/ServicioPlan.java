package com.programacion2.servicio;

import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.programacion2.dominio.Plan;
import com.programacion2.excepcion.ServicioException;
import com.programacion2.repositorio.IRepositorio;

@Service
public class ServicioPlan implements IServicioPlan {

    private final IRepositorio<Plan> repositorio;

    @Autowired
    public ServicioPlan(IRepositorio<Plan> repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public List<Plan> listar() {
        List<Plan> planes = repositorio.listar(Plan.class);
        planes.sort(Comparator.comparing(Plan::getPrecioMensual));
        return planes;
    }

    @Override
    public Plan consultar(int id) {
        Plan plan = repositorio.consultar(Plan.class, id);
        if (plan == null) {
            throw new ServicioException("No existe el plan elegido.");
        }
        return plan;
    }
}
