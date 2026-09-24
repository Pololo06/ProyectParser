package com.universidad.application.port;

import com.universidad.application.dto.estudiante.EstudianteActualizarDto;
import com.universidad.application.dto.estudiante.EstudianteCrearDto;
import com.universidad.application.dto.estudiante.EstudianteDto;

import java.util.List;
import java.util.UUID;

/**
 * Input port (use-case boundary) for Estudiante management.
 * Interface adapters (controllers) depend on this port, never on the
 * concrete service (Dependency Inversion).
 */
public interface EstudianteServicioPort {

    EstudianteDto registrarEstudiante(EstudianteCrearDto dto);

    List<EstudianteDto> obtenerEstudiantes();

    long contarEstudiantes();

    EstudianteDto actualizarEstudiante(EstudianteActualizarDto dto);

    boolean eliminarEstudiante(UUID id);
}
