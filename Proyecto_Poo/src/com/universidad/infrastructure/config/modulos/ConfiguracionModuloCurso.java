package com.universidad.infrastructure.config.modulos;

import com.cleandev.cli.core.SystemModule;
import com.cleandev.tpa.api.TpaRepository;
import com.cleandev.tpa.api.TpaRepositoryFactory;
import com.universidad.infrastructure.config.RutaPersistencia;
import com.universidad.interfaceadapters.controller.CursoControlador;
import com.universidad.application.mapper.CursoMapeador;
import com.universidad.domain.model.Curso;
import com.universidad.infrastructure.persistence.CursoRepositorioImpl;
import com.universidad.domain.repository.CursoRepositorio;
import com.universidad.application.service.CursoServicio;
import com.universidad.infrastructure.cli.CursoVista;
import java.util.UUID;

public class ConfiguracionModuloCurso implements ModuloConfigurable{
    private final CursoRepositorio repositorio;
    private final CursoServicio servicio;

    public ConfiguracionModuloCurso() {
        TpaRepository<Curso, UUID> tpaEngine = TpaRepositoryFactory.create(
                Curso.class, 
                RutaPersistencia.CURSOS.obtenerRuta(),
                true
        );
        
        CursoMapeador mapeador = new CursoMapeador();
        
        repositorio = new CursoRepositorioImpl(tpaEngine);
        servicio = new CursoServicio(mapeador, repositorio);
    }
    
    public CursoRepositorio getRepositorio() {
        return repositorio;
    }
    
    public CursoServicio getServicio() {
        return servicio;
    }

    @Override
    public SystemModule construirVista() {
        CursoControlador controlador = new CursoControlador(servicio);
        return new CursoVista(controlador);
    }

    @Override
    public void cerrarRecursos() {
        repositorio.cerrar();
    }
}
