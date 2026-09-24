package com.universidad.application.service;

import com.universidad.application.port.CursoServicioPort;
import java.util.UUID;

import com.universidad.domain.model.enumeracion.EstadoEntidad;

import com.universidad.application.dto.curso.CursoActualizarDto;

import com.universidad.application.dto.curso.CursoCrearDto;
import com.universidad.application.dto.curso.CursoDto;
import com.universidad.application.mapper.CursoMapeador;
import com.universidad.domain.model.Curso;
import com.universidad.domain.repository.CursoRepositorio;
import java.util.List;

public class CursoServicio implements CursoServicioPort {
    private final CursoMapeador mapeador;
    private final CursoRepositorio repositorio;

    public CursoServicio(CursoMapeador mapeador, CursoRepositorio repositorio) {
        if (mapeador == null) {
           throw new IllegalArgumentException("El mapeador es obligatorio");
        }
        
        if (repositorio == null) {
            throw new IllegalArgumentException("El repositorio es obligatorio");
        }
        
        this.mapeador = mapeador;
        this.repositorio = repositorio;
    }
    
    @Override
    public CursoDto registrarCurso(CursoCrearDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Los datos del curso son obligatorios");
        }
        
        Curso nuevo = new Curso(
                dto.codigoCurso(),
                dto.nombreCurso(),
                dto.creditosCurso(),
                dto.cupoMaximoCurso()
        );
        
        Curso guardaCurso = repositorio.guardar(nuevo);
        return mapeador.toDto(guardaCurso);
    }
    
    @Override
    public List<CursoDto> obtenerCursos() {
        return mapeador.toDtoList(repositorio.listarTodos());
    }
    
    @Override
    public long contarCursos() {
        return repositorio.contar();
    }
    
    
    @Override
    public CursoDto actualizarCurso(CursoActualizarDto dto) {
        if (dto == null) throw new IllegalArgumentException("Los datos son obligatorios");
        Curso actual = repositorio.buscarPorId(dto.id())
                .orElseThrow(() -> new IllegalArgumentException("No existe un curso con ese ID"));
        // Aplicar los cambios sobre una copia antes de persistirlos.
        Curso editado = new Curso(
                actual.getIdCurso(),
                actual.getCodigoCurso(),
                actual.getNombreCurso(),
                actual.getCreditosCurso(),
                actual.getCupoMaximoCurso(),
                actual.getEstadoCurso()
        );
        boolean huboCambios = false;
        if (dto.nuevoNombre() != null && !dto.nuevoNombre().equalsIgnoreCase(actual.getNombreCurso())) {
            editado.actualizarNombre(dto.nuevoNombre());
            huboCambios = true;
        }
        if (dto.nuevoCupo() != null && !dto.nuevoCupo().equals(actual.getCupoMaximoCurso())) {
            editado.actualizarCupo(dto.nuevoCupo());
            huboCambios = true;
        }
        EstadoEntidad estado = dto.obtenerEstadoComoEnum();
        if (estado != null && estado != actual.getEstadoCurso()) {
            editado.cambiarEstado(estado);
            huboCambios = true;
        }
        if (!huboCambios) throw new IllegalArgumentException("Los datos son iguales a los actuales");
        return mapeador.toDto(repositorio.actualizar(editado));
    }

    @Override
    public boolean eliminarCurso(UUID id) {
        if (id == null) throw new IllegalArgumentException("ID invalido");
        repositorio.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("No existe un curso con ese ID"));
        return repositorio.eliminar(id);
    }

}
