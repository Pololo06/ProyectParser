package com.universidad.application.service;

import com.universidad.application.port.AsignaturaServicioPort;
import com.universidad.application.dto.asignaturacosto.AsignaturaCostoCrearDto;
import com.universidad.application.dto.asignaturacosto.AsignaturaCostoDto;
import com.universidad.application.mapper.AsignaturaMapeador;
import com.universidad.domain.model.AsignaturaCosto;
import com.universidad.domain.repository.AsignaturaRepositorio;
import java.util.List;

public class AsignaturaServicio implements AsignaturaServicioPort {
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
    
    @Override
    public AsignaturaCostoDto registrarAsignatura(AsignaturaCostoCrearDto dto){
        if (dto == null) {
            throw new IllegalArgumentException("Los datos de la asignatura son obligatorios");
        }
        
        AsignaturaCosto nuevo = new AsignaturaCosto(dto.nombre(), dto.semanas(), dto.costoBase());
        
        AsignaturaCosto guardado = repositorio.guardar(nuevo);
        return mapeador.toDto(guardado);
    }
    
    @Override
    public List<AsignaturaCostoDto> obtenerAsignaturas() {
        return mapeador.toDtoList(repositorio.listarTodos());
    }
    
    @Override
    public int contarAsignaturas() {
        return (int)repositorio.contar();
    }
}
