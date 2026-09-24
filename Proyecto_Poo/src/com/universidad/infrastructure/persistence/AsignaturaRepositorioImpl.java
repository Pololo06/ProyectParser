package com.universidad.infrastructure.persistence;

import com.cleandev.tpa.api.TpaRepository;
import com.universidad.domain.model.AsignaturaCosto;
import com.universidad.domain.repository.AsignaturaRepositorio;

public class AsignaturaRepositorioImpl extends RepositorioBaseAbstracto<AsignaturaCosto, Integer> implements AsignaturaRepositorio{
    
    public AsignaturaRepositorioImpl(TpaRepository<AsignaturaCosto, Integer> tpaRepository) {
        super(tpaRepository);
    }
    
}
