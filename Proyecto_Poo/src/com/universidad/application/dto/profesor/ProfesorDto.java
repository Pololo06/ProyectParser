package com.universidad.application.dto.profesor;

import com.universidad.domain.model.enumeracion.EstadoEntidad;

public record ProfesorDto(
        Long id,
        String nombre,
        String celular,
        EstadoEntidad estado,
        Boolean activo
        ) {

}
