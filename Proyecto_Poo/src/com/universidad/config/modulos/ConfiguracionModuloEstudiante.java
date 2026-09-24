package com.universidad.config.modulos;

import com.cleandev.cli.core.SystemModule;
import com.cleandev.tpa.api.TpaRepository;
import com.cleandev.tpa.api.TpaRepositoryFactory;
import com.universidad.config.RutaPersistencia;
import com.universidad.controlador.EstudianteControlador;
import com.universidad.mapeador.EstudianteMapeador;
import com.universidad.modelo.Estudiante;
import com.universidad.persistencia.EstudianteRepositorioImpl;
import com.universidad.repositorio.EstudianteRepositorio;
import com.universidad.servicio.EstudianteServicio;
import com.universidad.vista.EstudianteVista;
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
