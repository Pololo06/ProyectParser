package com.universidad.interfaceadapters.controller;

import java.util.UUID;

import com.universidad.application.dto.estudiante.EstudianteActualizarDto;

import com.universidad.application.dto.estudiante.EstudianteCrearDto;
import com.universidad.application.dto.estudiante.EstudianteDto;
import com.universidad.application.port.EstudianteServicioPort;
import java.util.List;

public class EstudianteControlador {
    private final EstudianteServicioPort servicio;

    public EstudianteControlador(EstudianteServicioPort servicio) {
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
