package com.universidad.application.port;

import com.universidad.application.dto.curso.CursoActualizarDto;
import com.universidad.application.dto.curso.CursoCrearDto;
import com.universidad.application.dto.curso.CursoDto;

import java.util.List;
import java.util.UUID;

/**
 * Input port (use-case boundary) for Curso management.
 * Interface adapters (controllers) depend on this port, never on the
 * concrete service (Dependency Inversion).
 */
public interface CursoServicioPort {

    CursoDto registrarCurso(CursoCrearDto dto);

    List<CursoDto> obtenerCursos();

    long contarCursos();

    CursoDto actualizarCurso(CursoActualizarDto dto);

    boolean eliminarCurso(UUID id);
}
