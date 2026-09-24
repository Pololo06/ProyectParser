package com.universidad.config.modulos;

import com.cleandev.cli.core.SystemModule;
import com.cleandev.tpa.api.TpaRepository;
import com.cleandev.tpa.api.TpaRepositoryFactory;
import com.universidad.config.RutaPersistencia;
import com.universidad.controlador.CursoControlador;
import com.universidad.mapeador.CursoMapeador;
import com.universidad.modelo.Curso;
import com.universidad.persistencia.CursoRepositorioImpl;
import com.universidad.repositorio.CursoRepositorio;
import com.universidad.servicio.CursoServicio;
import com.universidad.vista.CursoVista;
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
