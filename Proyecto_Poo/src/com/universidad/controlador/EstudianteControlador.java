package com.universidad.controlador;

import java.util.UUID;

import com.universidad.dto.estudiante.EstudianteActualizarDto;

import com.universidad.dto.estudiante.EstudianteCrearDto;
import com.universidad.dto.estudiante.EstudianteDto;
import com.universidad.servicio.EstudianteServicio;
import java.util.List;

public class EstudianteControlador {
    private final EstudianteServicio servicio;

    public EstudianteControlador(EstudianteServicio servicio) {
        if (servicio == null) {
            throw new IllegalArgumentException("El servicio es obligatorio");
        }
        
        this.servicio = servicio;
    }
    
    public EstudianteDto crearEstudiante(EstudianteCrearDto dto) {
        return servicio.registrarEstudiante(dto);
    }
    
    public List<EstudianteDto> listarEstudiantes() {
        return servicio.obtenerEstudiantes();
    }
    
    public long cantidadEstudiantes() {
        return servicio.contarEstudiantes();
    }
    
    
    public EstudianteDto actualizarEstudiante(EstudianteActualizarDto dto) {
        return servicio.actualizarEstudiante(dto);
    }

    public boolean eliminarEstudiante(UUID id) {
        return servicio.eliminarEstudiante(id);
    }

}
