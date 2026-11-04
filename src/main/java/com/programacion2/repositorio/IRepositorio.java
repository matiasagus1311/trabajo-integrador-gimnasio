package com.programacion2.repositorio;

import java.util.List;

/**
 * Repositorio genérico (como el de clase): sirve para cualquier clase persistente.
 * Cuando se usa se reemplaza la T por la clase: IRepositorio<Socio>.
 */
public interface IRepositorio<T> {

    /** Guarda un objeto nuevo o actualiza uno desatado (detached). */
    void guardar(T objeto);

    /** Guarda varios juntos, en una sola transacción. */
    void guardar(List<T> objetos);

    /** Busca por id. Devuelve el objeto desatado, o null si no existe. */
    T consultar(Class<T> clase, int id);

    /** Ejecuta una named query (@Query de la clase) con un parámetro. */
    List<T> consultar(Class<T> clase, String nombreQuery, Object valor);

    /** Trae todos los objetos de esa clase, desatados. */
    List<T> listar(Class<T> clase);

    /** Borra de la base el objeto con ese id. */
    void eliminar(Class<T> clase, int id);
}
