package com.universidad.domain.repository.cargador;

import com.universidad.domain.model.Profesor;
import com.universidad.domain.repository.ProfesorRepositorio;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class ProfesorCargador implements CargadorDatos<Profesor, Long>{
    
    private final Map<Long, Profesor> cache;
    private final ProfesorRepositorio repositorio;

    public ProfesorCargador(ProfesorRepositorio repositorio) {
        
        if(repositorio == null) {
            throw new IllegalArgumentException("Ojo con el repo");
        }
        this.repositorio = repositorio;
        this.cache = new HashMap<>();
    }

    @Override
    public void cargarTodos() {
        this.cache.clear();
        for(Profesor profe : repositorio.listarTodos()){
            cache.put(profe.getIdProfesor(), profe);
        }
    }

    @Override
    public void cargarPorIds(List<Long> ids) {
        if(ids == null || ids.isEmpty()){
            return;
        }
        List<Long> idsFaltantes = new ArrayList<>();
        for (Long id: ids){
            if(!this.cache.containsKey(id)){
                idsFaltantes.add(id);
            }
        }
        
        if(idsFaltantes.isEmpty()){
            return;
        }
        Set<Long> idsFaltantesSet = new HashSet<>(idsFaltantes);
        for (Profesor profe : repositorio.listarTodos()){
            if(idsFaltantesSet.contains(profe.getIdProfesor())){
                cache.put(profe.getIdProfesor(), profe);
            }
        }
    }

    @Override
    public void registrarEnCache(Profesor entidad) {
        if(entidad != null && entidad.getIdProfesor() != null){
            cache.put(entidad.getIdProfesor(), entidad);
        }
    }

    @Override
    public Optional<Profesor> obtener(Long id) {
        if(id == null){
            return Optional.empty();
        }
        return Optional.ofNullable(cache.get(id));
    }

    @Override
    public boolean existe(Long id) {
        if(id==null){
            return false;
        }
        return cache.containsKey(id);
    }
    
    
    
    
    
    
}
