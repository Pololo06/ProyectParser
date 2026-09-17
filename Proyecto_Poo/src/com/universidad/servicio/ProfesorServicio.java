package com.universidad.servicio;

import com.universidad.dto.profesor.ProfesorActualizarDto;
import com.universidad.dto.profesor.ProfesorCrearDto;
import com.universidad.mapeador.ProfesorMapeador;
import com.universidad.repositorio.ProfesorRepositorio;
import com.universidad.dto.profesor.ProfesorDto;
import com.universidad.modelo.Profesor;
import com.universidad.modelo.enumeracion.EstadoEntidad;
import java.util.List;


//COCINERO
public class ProfesorServicio {
    private final ProfesorMapeador mapeador;
    private final ProfesorRepositorio repositorio;
    
    public ProfesorServicio(ProfesorMapeador mapeador, ProfesorRepositorio repositorio) {
        
        if(repositorio == null){
            throw new IllegalArgumentException("Pailas con el repo");
        }
        if(mapeador==null){
            throw new IllegalArgumentException("Error");
        }
        this.mapeador = mapeador;
        this.repositorio = repositorio;
    }
    
    public ProfesorDto registrarProfesor(ProfesorCrearDto dto){
        Profesor nuevoProfesor = new Profesor(dto.nombre(), dto.celular());
        Profesor guardado = repositorio.guardar(nuevoProfesor);
        return mapeador.toDto(guardado);
    }
    
    public List<ProfesorDto> obtenerProfesores() {
        return mapeador.toDtoList(repositorio.listarTodos());
    }
    
    public int contarProfesores() {
        return (int)repositorio.contar();
    }
    
    public ProfesorDto actualizarProfesor(ProfesorActualizarDto dto) {
        if (dto == null) throw new IllegalArgumentException("Los datos son obligatorios");
        Profesor actual = repositorio.buscarPorId(dto.id())
                .orElseThrow(() -> new IllegalArgumentException("No existe un profesor con ese ID"));
        // Aplicar los cambios sobre una copia antes de persistirlos.
        Profesor editado = new Profesor(
                actual.getIdProfesor(),
                actual.getNombreProfesor(),
                actual.getCelularProfesor(),
                actual.getEstadoProfesor()
        );
        boolean huboCambios = false;
        if (dto.nuevoNombre() != null && !dto.nuevoNombre().equalsIgnoreCase(actual.getNombreProfesor())) {
            editado.actualizarNombre(dto.nuevoNombre());
            huboCambios = true;
        }
        if (dto.nuevoCelular() != null && !dto.nuevoCelular().equalsIgnoreCase(actual.getCelularProfesor())) {
            editado.actualizarCelular(dto.nuevoCelular());
            huboCambios = true;
        }
        EstadoEntidad estado = dto.obtenerEstadoComoEnum();
        if (estado != null && estado != actual.getEstadoProfesor()) {
            editado.cambiarEstado(estado);
            huboCambios = true;
        }
        if (!huboCambios) throw new IllegalArgumentException("Los datos son iguales a los actuales");
        return mapeador.toDto(repositorio.actualizar(editado));
    }

    

    public boolean eliminarProfesor(Long id) {
        if (id == null || id <= 0) throw new IllegalArgumentException("ID invalido");
        repositorio.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("No existe un profesor con ese ID"));
        return repositorio.eliminar(id);
    }

}
