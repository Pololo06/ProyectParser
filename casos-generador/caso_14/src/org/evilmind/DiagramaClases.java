package org.evilmind;

import com.cleandev.umlgen.UmlGen;
import com.cleandev.umlgen.core.ports.UmlGenStorageException;
import com.cleandev.umlgen.infra.config.ExtractionConfig;
import com.cleandev.umlgen.infra.diagnostic.DiagnosticConfig;

import java.nio.file.Path;
import java.nio.file.Paths;

public class DiagramaClases {

    public static void main(String[] args) {

        try {
            Path directorioEntrada = Paths.get("src/org/evilmind");
            Path archivoSalida = Paths.get("src/clases.puml");

            ExtractionConfig reglasDeExtraccion = ExtractionConfig.builder()
                    // A. Excluir archivos o paquetes/clases del proyecto local
                    .excludeFromProject(
                            "DiagramaClases.java",
                            "edu.unimag.modelo.enumeracionX.*"
                    )
                    // B. Incluir tipos del JDK que normalmente estarían ocultos
                    .includeJdkTypes("java.util.concurrent.*")
                    // C. Excluir dominios o paquetes externos de terceros
                    .excludeExternalDomains("com.cleandev.tpaX.*")
                    .build();

            DiagnosticConfig configDiagnostico = DiagnosticConfig.debug();

            // 2. Instanciar la librería con AMBAS configuraciones
            UmlGen umlGen = UmlGen.withConfigs(configDiagnostico, reglasDeExtraccion);

            // 3. Ejecutar el pipeline completo
            umlGen.generateAndSave(directorioEntrada, archivoSalida);

            System.out.println("\nEl diagrama ha sido generado y guardado correctamente.");

        } catch (UmlGenStorageException e) {
            System.err.println("\nERROR DE ALMACENAMIENTO:");
            System.err.println("   Mensaje: " + e.getMessage());
            if (e.getCause() != null) {
                System.err.println("   Problema: " + e.getCause().getClass().getSimpleName() + " - " + e.getCause().getMessage());
            }
        } catch (IllegalArgumentException e) {
            System.err.println("\nERROR DE ARGUMENTOS:");
            System.err.println("   " + e.getMessage());
        } catch (Exception e) {
            System.err.println("\nERROR INESPERADO EN EL PIPELINE:");
            System.err.println("   " + e.getMessage());
        }
    }
}
