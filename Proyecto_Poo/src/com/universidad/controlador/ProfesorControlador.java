package com.universidad.controlador;

import com.universidad.dto.profesor.ProfesorActualizarDto;

import com.universidad.dto.profesor.ProfesorCrearDto;
import com.universidad.dto.profesor.ProfesorDto;
import com.universidad.servicio.ProfesorServicio;
import java.util.List;

//MESERO
public class ProfesorControlador {

    private final ProfesorServicio servicio;

    public ProfesorControlador(ProfesorServicio servicio) {
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
