package com.universidad.interfaceadapters.controller;

import java.util.UUID;

import com.universidad.application.dto.curso.CursoActualizarDto;

import com.universidad.application.dto.curso.CursoCrearDto;
import com.universidad.application.dto.curso.CursoDto;
import com.universidad.application.port.CursoServicioPort;
import java.util.List;

public class CursoControlador {

    private final CursoServicioPort servicio;

    public CursoControlador(CursoServicioPort servicio) {
        if (servicio == null) {
            throw new IllegalArgumentException("El servicio es obligatorio");
        }

        this.servicio = servicio;
    }

    public CursoDto crearCurso(CursoCrearDto dto) {
        return servicio.registrarCurso(dto);
    }

    /** @deprecated usar {@link #crearCurso(CursoCrearDto)}. */
    @Deprecated
    public CursoDto CrearCurso(CursoCrearDto dto) {
        return crearCurso(dto);
    }

    public List<CursoDto> listarCursos() {
        return servicio.obtenerCursos();
    }

    public long cantidadCursos() {
        return servicio.contarCursos();
    }

    public CursoDto actualizarCurso(CursoActualizarDto dto) {
        return servicio.actualizarCurso(dto);
    }

    public boolean eliminarCurso(UUID id) {
        return servicio.eliminarCurso(id);
    }

}
