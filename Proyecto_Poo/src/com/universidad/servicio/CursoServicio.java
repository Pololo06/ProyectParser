package com.universidad.servicio;

import java.util.UUID;

import com.universidad.modelo.enumeracion.EstadoEntidad;

import com.universidad.dto.curso.CursoActualizarDto;

import com.universidad.dto.curso.CursoCrearDto;
import com.universidad.dto.curso.CursoDto;
import com.universidad.mapeador.CursoMapeador;
import com.universidad.modelo.Curso;
import com.universidad.repositorio.CursoRepositorio;
import java.util.List;

public class CursoServicio {
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
    
    public List<CursoDto> obtenerCursos() {
        return mapeador.toDtoList(repositorio.listarTodos());
    }
    
    public long contarCursos() {
        return repositorio.contar();
    }
    
    
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

    public boolean eliminarCurso(UUID id) {
        if (id == null) throw new IllegalArgumentException("ID invalido");
        repositorio.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("No existe un curso con ese ID"));
        return repositorio.eliminar(id);
    }

}
