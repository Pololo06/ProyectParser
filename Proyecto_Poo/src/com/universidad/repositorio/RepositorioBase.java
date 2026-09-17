package com.universidad.repositorio;

import java.util.List;
import java.util.Optional;

public interface RepositorioBase<T, ID> {

    T guardar(T entidad);

    T actualizar(T entidad);

    Optional<T> buscarPorId(ID id);

    boolean eliminar(ID id);

    List<T> listarTodos();

    long contar();

    void cerrar();
}
