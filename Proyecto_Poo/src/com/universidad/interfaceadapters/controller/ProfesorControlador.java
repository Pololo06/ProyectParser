package com.universidad.interfaceadapters.controller;

import com.universidad.application.dto.profesor.ProfesorActualizarDto;

import com.universidad.application.dto.profesor.ProfesorCrearDto;
import com.universidad.application.dto.profesor.ProfesorDto;
import com.universidad.application.port.ProfesorServicioPort;
import java.util.List;

//MESERO
public class ProfesorControlador {

    private final ProfesorServicioPort servicio;

    public ProfesorControlador(ProfesorServicioPort servicio) {
        if (servicio == null) {
            throw new IllegalArgumentException("El servicio de profesores es obligatorio");
        }
        this.servicio = servicio;
    }

    public ProfesorDto crearProfesor(ProfesorCrearDto dto) {
        return servicio.registrarProfesor(dto);
    }

    public int cantidadProfesores() {
        return servicio.contarProfesores();
    }

    public List<ProfesorDto> listarProfesores() {
        return servicio.obtenerProfesores();
    }
    public ProfesorDto actualizarProfesor(ProfesorActualizarDto dto) {
        return servicio.actualizarProfesor(dto);
    }

    public boolean eliminarProfesor(Long id) {
        return servicio.eliminarProfesor(id);
    }

}
