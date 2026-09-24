package is.generador.infrastructure.javaparser;

import is.generador.application.AnalysisResult;
import is.generador.domain.model.ClassModel;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Recorre carpetas y parsea cada {@code .java} (paso 3 del split).
 * Acumula trazabilidad por corrida; el resultado viaja en el
 * {@link AnalysisResult} reutilizado en vez de campos mutables sueltos.
 */
class SourceScanner {

    private final TypeExtractor extractor;
    private final List<String> parsedFiles = new ArrayList<>();
    private final List<String> failedFiles = new ArrayList<>();
    private final List<String> failureReasons = new ArrayList<>();
    // Imports por clase (simpleName -> package), según el archivo donde se declaró.
    private final Map<String, Map<String, String>> fileImportsByClass = new HashMap<>();

    SourceScanner(TypeExtractor extractor) {
        this.extractor = extractor;
    }

    void traverseFolder(File folder, List<ClassModel> classes) throws IOException {
        File[] contents = folder.listFiles();
        if (contents == null) {
            return;
        }
        for (File file : contents) {
            if (file.isDirectory()) {
                traverseFolder(file, classes);
            } else if (file.getName().endsWith(".java")) {
                try {
                    classes.addAll(extractor.analyzeFile(file, fileImportsByClass));
                    parsedFiles.add(file.getPath());
                } catch (Exception e) {
                    String reason = e.getMessage() == null ? e.toString() : e.getMessage();
                    failedFiles.add(file.getPath());
                    failureReasons.add(file.getPath() + " -> " + reason);
                    // RNF-04/RNF-06: no System.err here; failures are exposed via
                    // getFailedFiles()/getFailureReasons()/getLastAnalysisSummary().
                }
            }
        }
    }

    Map<String, Map<String, String>> fileImportsByClass() {
        return fileImportsByClass;
    }

    /** Empaqueta la trazabilidad de esta corrida junto al proyecto ya construido. */
    AnalysisResult finish(is.generador.domain.model.ProjectModel project) {
        return new AnalysisResult(project,
                new ArrayList<>(parsedFiles),
                new ArrayList<>(failedFiles),
                new ArrayList<>(failureReasons));
    }
}
