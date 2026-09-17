package com.universidad.servicio;

import com.universidad.dto.asignaturacosto.AsignaturaCostoCrearDto;
import com.universidad.dto.asignaturacosto.AsignaturaCostoDto;
import com.universidad.mapeador.AsignaturaMapeador;
import com.universidad.modelo.AsignaturaCosto;
import com.universidad.repositorio.AsignaturaRepositorio;
import java.util.List;

public class AsignaturaServicio {
     private final AsignaturaMapeador mapeador;
    private final AsignaturaRepositorio repositorio;
    
    public AsignaturaServicio(AsignaturaMapeador mapeador, AsignaturaRepositorio repositorio) {
        
        if(repositorio == null){
            throw new IllegalArgumentException("Pailas con el repo");
        }
        if(mapeador==null){
            throw new IllegalArgumentException("Error");
        }
        this.mapeador = mapeador;
        this.repositorio = repositorio;
    }
    
    public AsignaturaCostoDto registrarAsignatura(AsignaturaCostoCrearDto dto){
        if (dto == null) {
            throw new IllegalArgumentException("Los datos de la asignatura son obligatorios");
        }
        
        AsignaturaCosto nuevo = new AsignaturaCosto(dto.nombre(), dto.semanas(), dto.costoBase());
        
        AsignaturaCosto guardado = repositorio.guardar(nuevo);
        return mapeador.toDto(guardado);
    }
    
    public List<AsignaturaCostoDto> obtenerAsignaturas() {
        return mapeador.toDtoList(repositorio.listarTodos());
    }
    
    public int contarAsignaturas() {
        return (int)repositorio.contar();
    }
}
