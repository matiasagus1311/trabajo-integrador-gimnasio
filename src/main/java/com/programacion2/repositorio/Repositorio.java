package com.programacion2.repositorio;

import java.util.ArrayList;
import java.util.List;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;
import javax.jdo.Transaction;

/**
 * Implementación del repositorio genérico con DataNucleus (JDO).
 * Todas las operaciones usan la misma estructura de clase:
 * begin -> operación -> commit; si algo falla, rollback y se avisa (throw);
 * y siempre se cierra el PersistenceManager.
 *
 * No lleva @Repository: los repositorios se crean como @Bean en
 * configuracion.Configuracion (uno por clase del dominio).
 */
public class Repositorio<T> implements IRepositorio<T> {

    // Cuántos niveles de relaciones se traen al consultar (ej.: actividad -> socios -> plan)
    private static final int PROFUNDIDAD = 2;

    // La fábrica se crea una sola vez (en Configuracion) y llega por el constructor
    private final PersistenceManagerFactory pmf;

    public Repositorio(PersistenceManagerFactory pmf) {
        this.pmf = pmf;
    }

    @Override
    public void guardar(T objeto) {
        PersistenceManager pm = pmf.getPersistenceManager();
        pm.setDetachAllOnCommit(true);   // al terminar, el objeto queda desatado CON sus datos
        Transaction tx = pm.currentTransaction();
        try {
            tx.begin();
            pm.makePersistent(objeto);   // INSERT si es nuevo, UPDATE si estaba desatado
            tx.commit();
        } catch (RuntimeException e) {
            if (tx.isActive()) {
                tx.rollback();           // se deshace todo
            }
            throw e;                     // y le avisa al servicio
        } finally {
            pm.close();
        }
    }

    @Override
    public void guardar(List<T> objetos) {
        PersistenceManager pm = pmf.getPersistenceManager();
        pm.setDetachAllOnCommit(true);   // al terminar, el objeto queda desatado CON sus datos
        Transaction tx = pm.currentTransaction();
        try {
            tx.begin();
            pm.makePersistentAll(objetos);   // todos con el MISMO pm y la misma transacción
            tx.commit();
        } catch (RuntimeException e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        } finally {
            pm.close();
        }
    }

    @Override
    public T consultar(Class<T> clase, int id) {
        PersistenceManager pm = pmf.getPersistenceManager();
        pm.setDetachAllOnCommit(true);                    // desatar al hacer commit
        pm.getFetchPlan().setMaxFetchDepth(PROFUNDIDAD);  // niveles de relaciones
        Transaction tx = pm.currentTransaction();
        try {
            tx.begin();
            T objeto = pm.getObjectById(clase, id);
            pm.retrieve(objeto);       // carga los campos antes de desatar
            tx.commit();               // acá queda detached
            return objeto;
        } catch (JDOObjectNotFoundException e) {
            return null;               // no existe ese id
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            pm.close();
        }
    }

    @Override
    public List<T> consultar(Class<T> clase, String nombreQuery, Object valor) {
        PersistenceManager pm = pmf.getPersistenceManager();
        pm.getFetchPlan().setMaxFetchDepth(PROFUNDIDAD);
        Transaction tx = pm.currentTransaction();
        Query<T> query = null;
        try {
            tx.begin();
            query = pm.newNamedQuery(clase, nombreQuery);
            query.setParameters(valor);
            List<T> resultados = query.executeList();
            pm.retrieveAll(resultados, true);                     // usa el FetchPlan
            List<T> separados = new ArrayList<>(pm.detachCopyAll(resultados));
            tx.commit();
            return separados;
        } catch (RuntimeException e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        } finally {
            if (query != null) {
                query.closeAll();
            }
            pm.close();
        }
    }

    @Override
    public List<T> listar(Class<T> clase) {
        PersistenceManager pm = pmf.getPersistenceManager();
        pm.getFetchPlan().setMaxFetchDepth(PROFUNDIDAD);
        Transaction tx = pm.currentTransaction();
        Query<T> query = null;
        try {
            tx.begin();
            query = pm.newQuery(clase);
            List<T> resultados = query.executeList();
            pm.retrieveAll(resultados, true);
            List<T> separados = new ArrayList<>(pm.detachCopyAll(resultados));
            tx.commit();
            return separados;
        } catch (RuntimeException e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        } finally {
            if (query != null) {
                query.closeAll();
            }
            pm.close();
        }
    }

    @Override
    public void eliminar(Class<T> clase, int id) {
        PersistenceManager pm = pmf.getPersistenceManager();
        Transaction tx = pm.currentTransaction();
        try {
            tx.begin();
            T objeto = pm.getObjectById(clase, id);
            pm.deletePersistent(objeto);   // DELETE (y sus filas en tablas intermedias)
            tx.commit();
        } catch (RuntimeException e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        } finally {
            pm.close();
        }
    }
}
