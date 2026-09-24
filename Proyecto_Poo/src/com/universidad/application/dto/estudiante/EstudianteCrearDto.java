package com.universidad.application.dto.estudiante;

import com.universidad.application.dto.validacion.ReglasValidacion;

public record EstudianteCrearDto(
        String codigo,
        String nombre,
        String correo,
        String celular,
        String direccion
        ) {

    public EstudianteCrearDto {
        codigo = ReglasValidacion.limpiarRequerido(codigo, "El codigo es obligatorio");

        nombre = ReglasValidacion.limpiarRequerido(nombre, "El nombre es obligatorioi");

        correo = ReglasValidacion.limpiarCorreo(correo);

        celular = ReglasValidacion.limpiarCelular(celular);

        direccion = ReglasValidacion.limpiarRequerido(direccion, "La direccion es obligatoria");
    }
}
