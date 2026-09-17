package com.universidad.config.modulos;

import com.cleandev.cli.core.SystemModule;
import com.cleandev.tpa.api.TpaRepository;
import com.cleandev.tpa.api.TpaRepositoryFactory;
import com.universidad.config.RutaPersistencia;
import com.universidad.controlador.ProfesorControlador;
import com.universidad.mapeador.ProfesorMapeador;
import com.universidad.modelo.Profesor;
import com.universidad.persistencia.ProfesorRepositorioImpl;
import com.universidad.repositorio.ProfesorRepositorio;
import com.universidad.servicio.ProfesorServicio;
import com.universidad.vista.ProfesorVista;

public class ConfiguracionModuloProfesor implements ModuloConfigurable {

    private final ProfesorRepositorio repositorio;
    private final ProfesorServicio servicio;

    public ConfiguracionModuloProfesor() {

        TpaRepository<Profesor, Long> tpaEngine = TpaRepositoryFactory.create(
                Profesor.class,
                RutaPersistencia.PROFESORES.obetenerRuta(),
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
