package com.universidad.persistencia;

import com.cleandev.tpa.api.TpaRepository;
import com.universidad.modelo.Profesor;
import com.universidad.repositorio.ProfesorRepositorio;

public class ProfesorRepositorioImpl extends RepositorioBaseAbstracto<Profesor, Long> implements ProfesorRepositorio {

    public ProfesorRepositorioImpl(TpaRepository<Profesor, Long> tpaRepository) {
        super(tpaRepository);
    }
    
    @Override
    protected boolean permiteBorradoFisico() {
        return true;
    }

}
