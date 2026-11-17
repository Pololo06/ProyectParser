package com.proyectparser.core.infrastructure.javaparser;

import com.proyectparser.core.domain.model.ClassModel;
import com.proyectparser.core.domain.model.ProjectModel;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.FileVisitResult;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.Path;
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
    // Imports por clase (fqn de la clase -> (simpleName -> package)), según el archivo donde se declaró.
    private final Map<String, Map<String, String>> fileImportsByClass = new HashMap<>();

    SourceScanner(TypeExtractor extractor) {
        this.extractor = extractor;
    }

    void traverseFolder(File folder, List<ClassModel> classes) throws IOException {
        List<Path> sources = new ArrayList<>();
        Path root = folder.toPath();
        // Do not follow symbolic links or Windows junctions (directory reparse points).
        Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
            @Override public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attributes) {
                return !dir.equals(root) && attributes.isOther()
                        ? FileVisitResult.SKIP_SUBTREE : FileVisitResult.CONTINUE;
            }
            @Override public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) {
                if (attributes.isRegularFile() && file.getFileName().toString().endsWith(".java")) {
                    sources.add(file);
                }
                return FileVisitResult.CONTINUE;
            }
        });
            for (Path path : sources.stream().sorted().toList()) {
                File file = path.toFile();
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

    Map<String, Map<String, String>> fileImportsByClass() {
        return fileImportsByClass;
    }

    /** Empaqueta la trazabilidad de esta corrida junto al proyecto ya construido. */
    AnalysisResult finish(ProjectModel project) {
        return new AnalysisResult(project, parsedFiles, failedFiles, failureReasons);
    }
}
