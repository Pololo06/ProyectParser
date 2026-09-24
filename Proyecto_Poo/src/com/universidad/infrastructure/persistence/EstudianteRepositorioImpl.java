package com.universidad.infrastructure.persistence;

import com.cleandev.tpa.api.TpaRepository;
import com.universidad.domain.model.Estudiante;
import com.universidad.domain.repository.EstudianteRepositorio;
import java.util.UUID;

public class EstudianteRepositorioImpl extends RepositorioBaseAbstracto<Estudiante, UUID> implements EstudianteRepositorio {

    public EstudianteRepositorioImpl(TpaRepository<Estudiante, UUID> tpaRepository) {
        super(tpaRepository);
    }
    
    @Override
    protected boolean permiteBorradoFisico() {
        return true;
    }

}
