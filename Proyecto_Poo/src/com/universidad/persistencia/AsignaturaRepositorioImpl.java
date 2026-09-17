package com.universidad.persistencia;

import com.cleandev.tpa.api.TpaRepository;
import com.universidad.modelo.AsignaturaCosto;
import com.universidad.repositorio.AsignaturaRepositorio;

public class AsignaturaRepositorioImpl extends RepositorioBaseAbstracto<AsignaturaCosto, Integer> implements AsignaturaRepositorio{
    
    public AsignaturaRepositorioImpl(TpaRepository<AsignaturaCosto, Integer> tpaRepository) {
        super(tpaRepository);
    }
    
}
