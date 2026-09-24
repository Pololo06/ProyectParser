package com.universidad.infrastructure.config;

import com.cleandev.cli.core.SystemModule;
import com.universidad.infrastructure.config.modulos.ConfiguracionModuloAsignatura;
import com.universidad.infrastructure.config.modulos.ConfiguracionModuloCurso;
import com.universidad.infrastructure.config.modulos.ConfiguracionModuloEstudiante;
import com.universidad.infrastructure.config.modulos.ConfiguracionModuloProfesor;
import com.universidad.infrastructure.config.modulos.ModuloConfigurable;
import java.util.ArrayList;
import java.util.List;

public class ConfiguracionDeDependencias implements AutoCloseable {

    private final List<SystemModule> modulos = new ArrayList<>();
    private final List<ModuloConfigurable> modulosConfigurados = new ArrayList<>();

    public ConfiguracionDeDependencias() {
        ConfiguracionModuloProfesor ConfiProfe = new ConfiguracionModuloProfesor();
        modulosConfigurados.add(ConfiProfe);
        modulos.add(ConfiProfe.construirVista());

        ConfiguracionModuloEstudiante ConfiEstudiante = new ConfiguracionModuloEstudiante();
        modulosConfigurados.add(ConfiEstudiante);
        modulos.add(ConfiEstudiante.construirVista());
        
        ConfiguracionModuloCurso ConfiCurso = new ConfiguracionModuloCurso();
        modulosConfigurados.add(ConfiCurso);
        modulos.add(ConfiCurso.construirVista());
        
        ConfiguracionModuloAsignatura ConfiAsignatura = new ConfiguracionModuloAsignatura();
        modulosConfigurados.add(ConfiAsignatura);
        modulos.add(ConfiAsignatura.construirVista());
    }

    public List<SystemModule> getModulos() {
        return modulos;
    }

    @Override
    public void close() throws Exception {
        for (int i = modulosConfigurados.size()-1; i >= 0; i--) {
            ModuloConfigurable modulito = modulosConfigurados.get(i);
            modulito.cerrarRecursos();
        }
        modulosConfigurados.clear();
    }

}
