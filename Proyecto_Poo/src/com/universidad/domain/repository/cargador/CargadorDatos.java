package com.universidad.domain.repository.cargador;

import java.util.List;
import java.util.Optional;

public interface CargadorDatos<T, ID> {

    void cargarTodos();

    void cargarPorIds(List<ID> ids);

    void registrarEnCache(T entidad);

    Optional<T> obtener(ID id);

    boolean existe(ID id);
}
