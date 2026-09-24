package com.universidad;

import com.cleandev.cli.config.CliConfig;
import com.cleandev.cli.core.CliEngine;
import com.cleandev.cli.core.SystemModule;
import com.universidad.infrastructure.config.ConfiguracionDeDependencias;
import java.util.List;


public class App {

    public static void main(String[] args) {
        try {
            iniciarAplicacion();
        } catch (Exception e) {
            System.err.println("ERROR: No se puede iniciar la App");
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    private static void iniciarAplicacion() throws Exception {
        CliConfig cliConfig = AppScannerCli.configuracionCompleta();
        CliEngine motorConsola = new CliEngine(cliConfig);
        
        try (ConfiguracionDeDependencias config = new ConfiguracionDeDependencias()) {
            registrarModulos(motorConsola, config);
            motorConsola.start();
        }
    }

    private static void registrarModulos(CliEngine motorConsola, ConfiguracionDeDependencias config) {
        List<SystemModule> modulos = config.getModulos();
        for (SystemModule modulo : modulos) {
            motorConsola.registerModule(modulo);
        }
    }
}
