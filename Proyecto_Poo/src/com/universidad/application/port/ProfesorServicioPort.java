package com.universidad.application.port;

import com.universidad.application.dto.profesor.ProfesorActualizarDto;
import com.universidad.application.dto.profesor.ProfesorCrearDto;
import com.universidad.application.dto.profesor.ProfesorDto;

import java.util.List;

/**
 * Input port (use-case boundary) for Profesor management.
 * Interface adapters (controllers) depend on this port, never on the
 * concrete service (Dependency Inversion).
 */
public interface ProfesorServicioPort {

    ProfesorDto registrarProfesor(ProfesorCrearDto dto);

    List<ProfesorDto> obtenerProfesores();

    int contarProfesores();

    ProfesorDto actualizarProfesor(ProfesorActualizarDto dto);

    boolean eliminarProfesor(Long id);
}
