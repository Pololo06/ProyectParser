package com.universidad.persistencia;

import com.cleandev.tpa.api.TpaRepository;
import com.universidad.modelo.Curso;
import com.universidad.repositorio.CursoRepositorio;
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
