package com.universidad.controlador;

import java.util.UUID;

import com.universidad.dto.curso.CursoActualizarDto;

import com.universidad.dto.curso.CursoCrearDto;
import com.universidad.dto.curso.CursoDto;
import com.universidad.servicio.CursoServicio;
import java.util.List;

public class CursoControlador {

    private final CursoServicio servicio;

    public CursoControlador(CursoServicio servicio) {
        if (servicio == null) {
            throw new IllegalArgumentException("El servicio es obligatorio");
        }

        this.servicio = servicio;
    }

    public CursoDto CrearCurso(CursoCrearDto dto) {
        return servicio.registrarCurso(dto);
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
