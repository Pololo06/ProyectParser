package com.universidad.dto.profesor;

import com.universidad.modelo.enumeracion.EstadoEntidad;

public record ProfesorDto(
        Long id,
        String nombre,
        String celular,
        EstadoEntidad estado,
        Boolean activo
        ) {

}
