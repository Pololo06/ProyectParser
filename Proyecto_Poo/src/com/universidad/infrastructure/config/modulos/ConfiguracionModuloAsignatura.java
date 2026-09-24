package com.universidad.infrastructure.config.modulos;

import com.cleandev.cli.core.SystemModule;
import com.cleandev.tpa.api.TpaRepository;
import com.cleandev.tpa.api.TpaRepositoryFactory;
import com.universidad.infrastructure.config.RutaPersistencia;
import com.universidad.interfaceadapters.controller.AsignaturaControlador;
import com.universidad.application.mapper.AsignaturaMapeador;
import com.universidad.domain.model.AsignaturaCosto;
import com.universidad.infrastructure.persistence.AsignaturaRepositorioImpl;
import com.universidad.domain.repository.AsignaturaRepositorio;
import com.universidad.application.service.AsignaturaServicio;
import com.universidad.infrastructure.cli.AsignaturaVista;

public class ConfiguracionModuloAsignatura implements ModuloConfigurable{
    private final AsignaturaRepositorio repositorio;
    private final AsignaturaServicio servicio;

    public ConfiguracionModuloAsignatura() {
        TpaRepository<AsignaturaCosto, Integer> tpaEngine = TpaRepositoryFactory.create(
                AsignaturaCosto.class, 
                RutaPersistencia.ASIGNATURAS.obtenerRuta(),
                true
        );
        
        AsignaturaMapeador mapeador = new AsignaturaMapeador();
        
        repositorio = new AsignaturaRepositorioImpl(tpaEngine);
        servicio = new AsignaturaServicio(mapeador, repositorio);
    }

    public AsignaturaRepositorio getRepositorio() {
        return repositorio;
    }

    public AsignaturaServicio getServicio() {
        return servicio;
    }
    
    @Override
    public SystemModule construirVista() {
        AsignaturaControlador controlador = new AsignaturaControlador(servicio);
        return new AsignaturaVista(controlador);
    }

    @Override
    public void cerrarRecursos() {
        repositorio.cerrar();
    }
}
