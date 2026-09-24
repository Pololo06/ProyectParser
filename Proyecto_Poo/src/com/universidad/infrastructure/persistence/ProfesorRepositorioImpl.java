package com.universidad.infrastructure.persistence;

import com.cleandev.tpa.api.TpaRepository;
import com.universidad.domain.model.Profesor;
import com.universidad.domain.repository.ProfesorRepositorio;

public class ProfesorRepositorioImpl extends RepositorioBaseAbstracto<Profesor, Long> implements ProfesorRepositorio {

    public ProfesorRepositorioImpl(TpaRepository<Profesor, Long> tpaRepository) {
        super(tpaRepository);
    }
    
    @Override
    protected boolean permiteBorradoFisico() {
        return true;
    }

}
