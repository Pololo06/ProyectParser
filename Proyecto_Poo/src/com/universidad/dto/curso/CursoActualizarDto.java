package com.universidad.dto.curso;

import com.universidad.dto.validacion.ReglasValidacion;
import com.universidad.modelo.enumeracion.EstadoEntidad;
import java.util.UUID;

public record CursoActualizarDto(
        UUID id,
        String nuevoNombre,
        Integer nuevoCupo,
        Integer nuevoEstado
        ) {

    public CursoActualizarDto {
        id = ReglasValidacion.limpiarUuidRequerido(id, "El ID es obligatorio");
        if (nuevoNombre != null) {
            nuevoNombre = nuevoNombre.isBlank() ? null : nuevoNombre.trim();
        }
        if (nuevoCupo != null) {
            nuevoCupo = ReglasValidacion.limpiarEnteroPositivo(nuevoCupo, "El cupo debe ser mayor que cero");
        }
        if (nuevoEstado != null) {
            nuevoEstado = ReglasValidacion.limpiarEnteroEnRango(nuevoEstado, 1, 2, "Estado: 1-Activo o 2-Inactivo");
        }
        if (nuevoNombre == null && nuevoCupo == null && nuevoEstado == null) {
            throw new IllegalArgumentException("Debes indicar al menos un cambio");
        }
    }

    public EstadoEntidad obtenerEstadoComoEnum() {
        if (nuevoEstado == null) {
            return null;
        }
        return nuevoEstado == 1 ? EstadoEntidad.ACTIVO : EstadoEntidad.INACTIVO;
    }
}
