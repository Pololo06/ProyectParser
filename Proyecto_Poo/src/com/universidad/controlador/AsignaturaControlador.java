package com.universidad.controlador;

import com.universidad.dto.asignaturacosto.AsignaturaCostoCrearDto;
import com.universidad.dto.asignaturacosto.AsignaturaCostoDto;
import com.universidad.servicio.AsignaturaServicio;
import java.util.List;

public class AsignaturaControlador {
    private final AsignaturaServicio servicio;

    public AsignaturaControlador(AsignaturaServicio servicio) {
        if (servicio == null) {
            throw new IllegalArgumentException("Ojo con el controlador");
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
