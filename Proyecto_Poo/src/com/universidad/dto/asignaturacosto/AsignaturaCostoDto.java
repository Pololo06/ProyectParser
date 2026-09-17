package com.universidad.dto.asignaturacosto;

import com.universidad.modelo.enumeracion.EstadoEntidad;

import java.math.BigDecimal;

public record AsignaturaCostoDto(
        Integer id,
        String nombre,
        Short semanas,
        BigDecimal costoBase,
        EstadoEntidad estado
        ) {

}
