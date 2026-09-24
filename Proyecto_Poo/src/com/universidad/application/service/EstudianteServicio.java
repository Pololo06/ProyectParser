package com.universidad.application.service;

import com.universidad.application.port.EstudianteServicioPort;
import java.util.UUID;

import com.universidad.domain.model.enumeracion.EstadoEntidad;

import com.universidad.application.dto.estudiante.EstudianteActualizarDto;

import com.universidad.application.dto.estudiante.EstudianteCrearDto;
import com.universidad.application.dto.estudiante.EstudianteDto;
import com.universidad.application.mapper.EstudianteMapeador;
import com.universidad.domain.model.Estudiante;
import com.universidad.domain.repository.EstudianteRepositorio;
import java.util.List;

public class EstudianteServicio implements EstudianteServicioPort {
    private final EstudianteMapeador mapeador;
    private final EstudianteRepositorio repositorio;

    public EstudianteServicio(EstudianteMapeador mapeador, EstudianteRepositorio repositorio) {
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
    public EstudianteDto registrarEstudiante(EstudianteCrearDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Los datos del estudiante son obligatorios");
        }
        
        Estudiante nuevo = new Estudiante(
                dto.codigo(),
                dto.nombre(),
                dto.correo(),
                dto.celular(),
                dto.direccion()
        );
        
        Estudiante guardado = repositorio.guardar(nuevo);
        return mapeador.toDto(guardado);
    }
    
    @Override
    public List<EstudianteDto> obtenerEstudiantes() {
        return mapeador.toDtoList(repositorio.listarTodos());
    }
    
    @Override
    public long contarEstudiantes() {
        return repositorio.contar();
    }
    
    @Override
    public EstudianteDto actualizarEstudiante(EstudianteActualizarDto dto) {
        if (dto == null) throw new IllegalArgumentException("Los datos son obligatorios");
        Estudiante actual = repositorio.buscarPorId(dto.id())
                .orElseThrow(() -> new IllegalArgumentException("No existe un estudiante con ese ID"));
        // Aplicar los cambios sobre una copia antes de persistirlos.
        Estudiante editado = new Estudiante(
                actual.getIdEstudiante(),
                actual.getCodigoEstudiante(),
                actual.getNombreEstudiante(),
                actual.getCorreoEstudiante(),
                actual.getCelularEstudiante(),
                actual.getDireccionEstudiante(),
                actual.getEstadoEstudiante()
        );
        boolean huboCambios = false;
        if (dto.nuevoCorreo() != null && !dto.nuevoCorreo().equalsIgnoreCase(actual.getCorreoEstudiante())) {
            editado.actualizarCorreo(dto.nuevoCorreo());
            huboCambios = true;
        }
        if (dto.nuevoCelular() != null && !dto.nuevoCelular().equals(actual.getCelularEstudiante())) {
            editado.actualizarCelular(dto.nuevoCelular());
            huboCambios = true;
        }
        if (dto.nuevaDireccion() != null && !dto.nuevaDireccion().equalsIgnoreCase(actual.getDireccionEstudiante())) {
            editado.actualizarDireccion(dto.nuevaDireccion());
            huboCambios = true;
        }
        EstadoEntidad estado = dto.obtenerEstadoComoEnum();
        if (estado != null && estado != actual.getEstadoEstudiante()) {
            editado.cambiarEstado(estado);
            huboCambios = true;
        }
        if (!huboCambios) throw new IllegalArgumentException("Los datos son iguales a los actuales");
        return mapeador.toDto(repositorio.actualizar(editado));
    }

    @Override
    public boolean eliminarEstudiante(UUID id) {
        if (id == null) throw new IllegalArgumentException("ID invalido");
        repositorio.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("No existe un estudiante con ese ID"));
        return repositorio.eliminar(id);
    }

}
