package com.universidad.dto.asignaturacosto;

import com.universidad.dto.validacion.ReglasValidacion;
import com.universidad.modelo.constante.PoliticasAcademicas;
import java.math.BigDecimal;

public record AsignaturaCostoActualizarDto(
        Integer id,
        String nuevoNombre,
        Short nuevasSemanas,
        BigDecimal nuevoCosto
        ) {

    public AsignaturaCostoActualizarDto {

        if (id == null) {
            throw new IllegalArgumentException("El ID de la asignatura costo es obligatorio para actualizar");
        }

        if (nuevoNombre != null) {
            nuevoNombre = ReglasValidacion.limpiarRequerido(
                    nuevoNombre,
                    "El nuevo nombre no puede estar vacío"
            );
        }

        if (nuevasSemanas != null) {
            nuevasSemanas = validarSemanas(nuevasSemanas);
        }

        if (nuevoCosto != null) {
            nuevoCosto = ReglasValidacion.limpiarBigDecimalPositivo(
                    nuevoCosto,
                    "El nuevo costo debe ser un valor positivo"
            );
        }
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
