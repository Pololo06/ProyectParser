package com.universidad.infrastructure.config.modulos;

import com.cleandev.cli.core.SystemModule;
import com.cleandev.tpa.api.TpaRepository;
import com.cleandev.tpa.api.TpaRepositoryFactory;
import com.universidad.infrastructure.config.RutaPersistencia;
import com.universidad.interfaceadapters.controller.EstudianteControlador;
import com.universidad.application.mapper.EstudianteMapeador;
import com.universidad.domain.model.Estudiante;
import com.universidad.infrastructure.persistence.EstudianteRepositorioImpl;
import com.universidad.domain.repository.EstudianteRepositorio;
import com.universidad.application.service.EstudianteServicio;
import com.universidad.infrastructure.cli.EstudianteVista;
import java.util.UUID;

public class ConfiguracionModuloEstudiante implements ModuloConfigurable {
    
    private final EstudianteRepositorio repositorio;
    private final EstudianteServicio servicio;

    public ConfiguracionModuloEstudiante() {
        TpaRepository<Estudiante, UUID> tpaEngine = TpaRepositoryFactory.create(
                Estudiante.class, 
                RutaPersistencia.ESTUDIANTES.obtenerRuta(),
                true
        );
        
        EstudianteMapeador mapeador = new EstudianteMapeador();
        
        repositorio = new EstudianteRepositorioImpl(tpaEngine);
        servicio = new EstudianteServicio(mapeador, repositorio);
    }
    
    public EstudianteRepositorio getRepositorio() {
        return repositorio;
    }

    public EstudianteServicio getServicio() {
        return servicio;
    }

    @Override
    public SystemModule construirVista() {
        EstudianteControlador controlador = new EstudianteControlador(servicio);
        return new EstudianteVista(controlador);
    }   

    @Override
    public void cerrarRecursos() {
        repositorio.cerrar();
    }
    
    
    
}
