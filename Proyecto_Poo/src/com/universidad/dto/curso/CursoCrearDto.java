package com.universidad.dto.curso;

import com.universidad.dto.validacion.ReglasValidacion;

public record CursoCrearDto(
        String codigoCurso,
        String nombreCurso,
        Integer creditosCurso,
        Integer cupoMaximoCurso
        ) {

    public CursoCrearDto {
        codigoCurso = ReglasValidacion.limpiarRequerido(codigoCurso, "El codigo del curso es nesesario");

        nombreCurso = ReglasValidacion.limpiarRequerido(nombreCurso, "El nombre es obligatorio");

        creditosCurso = ReglasValidacion.limpiarEnteroEnRango(creditosCurso, 1, 6, "Los creditos son obligatorios");

        cupoMaximoCurso = ReglasValidacion.limpiarEnteroPositivo(cupoMaximoCurso, "Requiere cantidad de Cupos");
    }

}
