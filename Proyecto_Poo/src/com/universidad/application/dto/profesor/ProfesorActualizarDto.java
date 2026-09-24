package com.universidad.application.dto.profesor;

import com.universidad.application.dto.validacion.ReglasValidacion;
import com.universidad.domain.model.enumeracion.EstadoEntidad;

public record ProfesorActualizarDto(Long id, String nuevoNombre, String nuevoCelular, Integer nuevoEstado) {

    public ProfesorActualizarDto {
        id = ReglasValidacion.limpiarLongRequerido(id, "id es obligatorio");

        if (nuevoNombre != null) {
            nuevoNombre = nuevoNombre.isBlank() ? null : nuevoNombre.trim();
        }

        if (nuevoCelular != null) {
            if (nuevoCelular.isBlank()) {
                nuevoCelular = null;
            } else {
                nuevoCelular = ReglasValidacion.limpiarCelular(nuevoCelular);
            }
        }

        if (nuevoEstado != null) {
            if (nuevoEstado != EstadoEntidad.ACTIVO.getCode() && nuevoEstado != EstadoEntidad.INACTIVO.getCode()) {
                throw new IllegalArgumentException("Solo acepto 1 y 2");
            }
        }

        if (nuevoNombre == null && nuevoCelular == null && nuevoEstado == null) {
            throw new IllegalArgumentException("No viene nada!!!");
        }
    }

    public EstadoEntidad obtenerEstadoComoEnum() {
        if (nuevoEstado == null) {
            return null;
        }
        if (nuevoEstado == EstadoEntidad.ACTIVO.getCode()) {
            return EstadoEntidad.ACTIVO;
        }
        return EstadoEntidad.INACTIVO;
    }
}
