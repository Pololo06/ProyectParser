package com.universidad.dto.estudiante;

import com.universidad.modelo.enumeracion.EstadoEntidad;
import java.util.UUID;

public record EstudianteDto(
        UUID id,
        String codigo,
        String nombre,
        String correo,
        String celular,
        String direccion,
        EstadoEntidad estado,
        boolean activo
        ) {

}
