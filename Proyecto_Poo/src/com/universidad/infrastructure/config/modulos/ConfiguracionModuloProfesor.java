package com.universidad.infrastructure.config.modulos;

import com.cleandev.cli.core.SystemModule;
import com.cleandev.tpa.api.TpaRepository;
import com.cleandev.tpa.api.TpaRepositoryFactory;
import com.universidad.infrastructure.config.RutaPersistencia;
import com.universidad.interfaceadapters.controller.ProfesorControlador;
import com.universidad.application.mapper.ProfesorMapeador;
import com.universidad.domain.model.Profesor;
import com.universidad.infrastructure.persistence.ProfesorRepositorioImpl;
import com.universidad.domain.repository.ProfesorRepositorio;
import com.universidad.application.service.ProfesorServicio;
import com.universidad.infrastructure.cli.ProfesorVista;

public class ConfiguracionModuloProfesor implements ModuloConfigurable {

    private final ProfesorRepositorio repositorio;
    private final ProfesorServicio servicio;

    public ConfiguracionModuloProfesor() {

        TpaRepository<Profesor, Long> tpaEngine = TpaRepositoryFactory.create(
                Profesor.class,
                RutaPersistencia.PROFESORES.obtenerRuta(),
                false);

        ProfesorMapeador mapeador = new ProfesorMapeador();
        repositorio = new ProfesorRepositorioImpl(tpaEngine);
        servicio = new ProfesorServicio(mapeador, repositorio);
    }

    public ProfesorRepositorio getRepositorio() {
        return repositorio;
    }

    public ProfesorServicio getServicio() {
        return servicio;
    }

    @Override
    public SystemModule construirVista() {
        ProfesorControlador controlador = new ProfesorControlador(servicio);
        return new ProfesorVista(controlador);
    }

    @Override
    public void cerrarRecursos() {
        if (repositorio != null) {
            repositorio.cerrar();
        }
    }

}
