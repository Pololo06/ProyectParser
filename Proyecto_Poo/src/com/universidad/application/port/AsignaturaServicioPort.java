package com.universidad.application.port;

import com.universidad.application.dto.asignaturacosto.AsignaturaCostoCrearDto;
import com.universidad.application.dto.asignaturacosto.AsignaturaCostoDto;

import java.util.List;

/**
 * Input port (use-case boundary) for AsignaturaCosto management.
 * Interface adapters (controllers) depend on this port, never on the
 * concrete service (Dependency Inversion).
 */
public interface AsignaturaServicioPort {

    AsignaturaCostoDto registrarAsignatura(AsignaturaCostoCrearDto dto);

    List<AsignaturaCostoDto> obtenerAsignaturas();

    int contarAsignaturas();
}
