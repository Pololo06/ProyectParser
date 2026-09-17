package com.universidad.config.modulos;

import com.cleandev.cli.core.SystemModule;
import com.cleandev.tpa.api.TpaRepository;
import com.cleandev.tpa.api.TpaRepositoryFactory;
import com.universidad.config.RutaPersistencia;
import com.universidad.controlador.AsignaturaControlador;
import com.universidad.mapeador.AsignaturaMapeador;
import com.universidad.modelo.AsignaturaCosto;
import com.universidad.persistencia.AsignaturaRepositorioImpl;
import com.universidad.repositorio.AsignaturaRepositorio;
import com.universidad.servicio.AsignaturaServicio;
import com.universidad.vista.AsignaturaVista;

public class ConfiguracionModuloAsignatura implements ModuloConfigurable{
    private final AsignaturaRepositorio repositorio;
    private final AsignaturaServicio servicio;

    public ConfiguracionModuloAsignatura() {
        TpaRepository<AsignaturaCosto, Integer> tpaEngine = TpaRepositoryFactory.create(
                AsignaturaCosto.class, 
                RutaPersistencia.ASIGNATURAS.obetenerRuta(),
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
