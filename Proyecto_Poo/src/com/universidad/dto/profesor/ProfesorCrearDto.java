package com.universidad.dto.profesor;

import com.universidad.dto.validacion.ReglasValidacion;

public record ProfesorCrearDto(String nombre, String celular) {

    public ProfesorCrearDto {
        nombre = ReglasValidacion.limpiarRequerido(nombre, "Nombre es obligatorio");
        celular = ReglasValidacion.limpiarCelular(celular);
    }
}
