package com.universidad.mapeador;

import com.universidad.dto.curso.CursoDto;
import com.universidad.modelo.Curso;
import java.util.ArrayList;
import java.util.List;

public class CursoMapeador implements Mapeador<Curso, CursoDto> {

    @Override
    public CursoDto toDto(Curso entidad) {
        if (entidad == null) {
            throw new IllegalArgumentException("El curso es obligatorio");
        }
        return new CursoDto(
                entidad.getIdCurso(),
                entidad.getCodigoCurso(),
                entidad.getNombreCurso(),
                entidad.getCreditosCurso(),
                entidad.getCupoMaximoCurso(),
                entidad.getEstadoCurso(),
                entidad.estaActivo()
        );
    }

    @Override
    public List<CursoDto> toDtoList(List<Curso> entidades) {
        if (entidades == null || entidades.isEmpty()) {
            return List.of();
        }

        List<CursoDto> resultado = new ArrayList<>(entidades.size());

        for (Curso curso : entidades) {
            resultado.add(toDto(curso));
        }

        return resultado;
    }

}
