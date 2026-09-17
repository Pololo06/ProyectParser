package com.universidad.servicio;

import java.util.UUID;

import com.universidad.modelo.enumeracion.EstadoEntidad;

import com.universidad.dto.estudiante.EstudianteActualizarDto;

import com.universidad.dto.estudiante.EstudianteCrearDto;
import com.universidad.dto.estudiante.EstudianteDto;
import com.universidad.mapeador.EstudianteMapeador;
import com.universidad.modelo.Estudiante;
import com.universidad.repositorio.EstudianteRepositorio;
import java.util.List;

public class EstudianteServicio {
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
    
    public List<EstudianteDto> obtenerEstudiantes() {
        return mapeador.toDtoList(repositorio.listarTodos());
    }
    
    public long contarEstudiantes() {
        return repositorio.contar();
    }
    
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

    public boolean eliminarEstudiante(UUID id) {
        if (id == null) throw new IllegalArgumentException("ID invalido");
        repositorio.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("No existe un estudiante con ese ID"));
        return repositorio.eliminar(id);
    }

}
