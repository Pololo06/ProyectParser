package com.universidad.application.dto.curso;

import com.universidad.domain.model.enumeracion.EstadoEntidad;
import java.util.UUID;

public record CursoDto(
        UUID id,
        String codigoCurso,
        String nombreCurso,
        Integer creditosCurso,
        Integer cupoMaximoCurso,
        EstadoEntidad estado,
        boolean activo
        ) {

}
