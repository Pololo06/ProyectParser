package com.universidad.dto.asignaturacosto;

import com.universidad.dto.validacion.ReglasValidacion;
import com.universidad.modelo.constante.PoliticasAcademicas;
import java.math.BigDecimal;

public record AsignaturaCostoCrearDto(
        String nombre,
        Short semanas,
        BigDecimal costoBase
        ) {

    public AsignaturaCostoCrearDto {

        nombre = ReglasValidacion.limpiarRequerido(
                nombre,
                "El nombre de la asignatura es obligatorio"
        );

        semanas = validarSemanas(semanas);

        costoBase = ReglasValidacion.limpiarBigDecimalPositivo(
                costoBase,
                "El costo base debe ser un valor positivo"
        );
    }

    private static Short validarSemanas(Short semanas) {
        int valor = semanas;
        valor = ReglasValidacion.limpiarEnteroEnRango(
                valor,
                PoliticasAcademicas.MIN_SEMANAS_ASIGNATURA,
                PoliticasAcademicas.MAX_SEMANAS_ASIGNATURA,
                "Las semanas deben estar entre "
                + PoliticasAcademicas.MIN_SEMANAS_ASIGNATURA
                + " y "
                + PoliticasAcademicas.MAX_SEMANAS_ASIGNATURA
        );
        return (short) valor;
    }
}
