package com.universidad.dto.curso;

import com.universidad.modelo.enumeracion.EstadoEntidad;
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
