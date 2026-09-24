package com.universidad.application.dto.asignaturacosto;

import com.universidad.domain.model.enumeracion.EstadoEntidad;

import java.math.BigDecimal;

public record AsignaturaCostoDto(
        Integer id,
        String nombre,
        Short semanas,
        BigDecimal costoBase,
        EstadoEntidad estado
        ) {

}
