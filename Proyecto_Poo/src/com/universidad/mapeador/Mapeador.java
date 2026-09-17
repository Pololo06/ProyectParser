package com.universidad.mapeador;

import java.util.List;

public interface Mapeador<E, R> {

    R toDto(E entidad);

    List<R> toDtoList(List<E> entidades);
}
