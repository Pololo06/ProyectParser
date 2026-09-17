package com.universidad.persistencia;

import com.cleandev.tpa.api.TpaRepository;
import com.universidad.modelo.Estudiante;
import com.universidad.repositorio.EstudianteRepositorio;
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
