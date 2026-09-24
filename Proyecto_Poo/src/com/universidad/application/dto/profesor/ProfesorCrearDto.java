package com.universidad.application.dto.profesor;

import com.universidad.application.dto.validacion.ReglasValidacion;

public record ProfesorCrearDto(String nombre, String celular) {

    public ProfesorCrearDto {
        nombre = ReglasValidacion.limpiarRequerido(nombre, "Nombre es obligatorio");
        celular = ReglasValidacion.limpiarCelular(celular);
    }
}
