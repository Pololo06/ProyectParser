package com.universidad.application.dto.estudiante;

import com.universidad.application.dto.validacion.ReglasValidacion;
import com.universidad.domain.model.enumeracion.EstadoEntidad;
import java.util.UUID;

public record EstudianteActualizarDto(
        UUID id,
        String nuevoCorreo,
        String nuevoCelular,
        String nuevaDireccion,
        Integer nuevoEstado
        ) {

    public EstudianteActualizarDto {
        id = ReglasValidacion.limpiarUuidRequerido(id, "El ID es obligatorio");
        if (nuevoCorreo != null) {
            nuevoCorreo = nuevoCorreo.isBlank() ? null : ReglasValidacion.limpiarCorreo(nuevoCorreo);
        }
        if (nuevoCelular != null) {
            nuevoCelular = nuevoCelular.isBlank() ? null : ReglasValidacion.limpiarCelular(nuevoCelular);
        }
        if (nuevaDireccion != null) {
            nuevaDireccion = nuevaDireccion.isBlank() ? null : nuevaDireccion.trim();
        }
        if (nuevoEstado != null) {
            nuevoEstado = ReglasValidacion.limpiarEnteroEnRango(nuevoEstado, 1, 2, "Estado: 1 Activo o 2 Inactivo");
        }
        if (nuevoCorreo == null && nuevoCelular == null && nuevaDireccion == null && nuevoEstado == null) {
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
