package com.universidad.mapeador;

import com.universidad.dto.profesor.ProfesorDto;
import com.universidad.modelo.Profesor;
import java.util.ArrayList;
import java.util.List;

public class ProfesorMapeador implements Mapeador<Profesor, ProfesorDto> {

    public ProfesorMapeador() {
    }

    @Override
    public ProfesorDto toDto(Profesor entidad) {
        if (entidad == null) {
            throw new IllegalArgumentException("Profesor requerido");
        }
        return new ProfesorDto(
                entidad.getIdProfesor(),
                entidad.getNombreProfesor(),
                entidad.getCelularProfesor(),
                entidad.getEstadoProfesor(),
                entidad.estaActivo());
    }

    @Override
    public List<ProfesorDto> toDtoList(List<Profesor> entidades) {
        if (entidades == null || entidades.isEmpty()) {
            return List.of();
        }
        List<ProfesorDto> resultado = new ArrayList<>(entidades.size());
        for (Profesor profe : entidades) {
            resultado.add(toDto(profe));
        }
        return resultado;
    }
}
