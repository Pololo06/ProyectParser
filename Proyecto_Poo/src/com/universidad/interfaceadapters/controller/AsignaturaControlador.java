package com.universidad.interfaceadapters.controller;

import com.universidad.application.dto.asignaturacosto.AsignaturaCostoCrearDto;
import com.universidad.application.dto.asignaturacosto.AsignaturaCostoDto;
import com.universidad.application.port.AsignaturaServicioPort;
import java.util.List;

public class AsignaturaControlador {
    private final AsignaturaServicioPort servicio;

    public AsignaturaControlador(AsignaturaServicioPort servicio) {
        if (servicio == null) {
            throw new IllegalArgumentException("El servicio es obligatorio");
        }
        this.servicio = servicio;
    }

    public AsignaturaCostoDto crearAsignatura(AsignaturaCostoCrearDto dto) {
        return servicio.registrarAsignatura(dto);
    }
    
    public int cantidadAsignatura() {
        return servicio.contarAsignaturas();
    }
    
    public List<AsignaturaCostoDto> listarAsignatura() {
        return servicio.obtenerAsignaturas();
    }
}
