package com.universidad.application.mapper;

import com.universidad.application.dto.asignaturacosto.AsignaturaCostoDto;
import com.universidad.domain.model.AsignaturaCosto;
import java.util.ArrayList;
import java.util.List;

public class AsignaturaMapeador implements Mapeador<AsignaturaCosto, AsignaturaCostoDto> {

    public AsignaturaMapeador() {
    }

    @Override
    public AsignaturaCostoDto toDto(AsignaturaCosto entidad) {
        if (entidad == null) {
            throw new IllegalArgumentException("asignatura Requerida");
        }
        return new AsignaturaCostoDto(entidad.getIdAsignaturaCosto(), entidad.getNombreAsignatura(), entidad.getSemanasDuracion(), entidad.getCostoBase(), entidad.getEstadoAsignatura());              
        
    }

    @Override
    public List<AsignaturaCostoDto> toDtoList(List<AsignaturaCosto> entidades) {
        if (entidades == null || entidades.isEmpty()) {
            return List.of();
        }
        List<AsignaturaCostoDto> resultado = new ArrayList<>(entidades.size());
        for (AsignaturaCosto asignaturaCosto : entidades) {
            resultado.add(toDto(asignaturaCosto));
        }
        return resultado;
    }

}