package com.universidad.infrastructure.persistence;

import com.cleandev.tpa.api.TpaRepository;
import com.universidad.domain.model.Curso;
import com.universidad.domain.repository.CursoRepositorio;
import java.util.UUID;

public class CursoRepositorioImpl extends RepositorioBaseAbstracto<Curso, UUID> implements CursoRepositorio {
    
    public CursoRepositorioImpl(TpaRepository<Curso, UUID> tpaRepository) {
        super(tpaRepository);
    }
    
    @Override
    protected boolean permiteBorradoFisico() {
        return true;
    }

}
