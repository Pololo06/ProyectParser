package com.universidad.infrastructure.config.modulos;

import com.cleandev.cli.core.SystemModule;

public interface ModuloConfigurable {

    SystemModule construirVista();

    void cerrarRecursos();
    
}
