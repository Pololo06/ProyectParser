package is.generador.infrastructure.javaparser;

import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.StaticJavaParser;
import is.generador.domain.model.ClassModel;
import is.generador.domain.model.ProjectModel;
import is.generador.domain.model.RelationshipModel;

import java.io.File;
import java.io.IOException;
import is.generador.domain.port.RunStatsProvider;
import is.generador.domain.port.SourceAnalyzerPort;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Analyzes a Java source tree with JavaParser and builds the internal domain model.
 *
 * <p>Thin orchestrator (public API): {@link SourceScanner} walks files,
 * {@link TypeExtractor} builds {@code ClassModel}s, {@link RelationshipDetector}
 * deduces relations and {@link ExternalTypeRegistrar} adds {@code @external} boxes,
 * both resolving type references through one {@link TypeResolver}.
 * Covers nested types at any depth, generic arguments and enum constants.
 *
 * <p>Stateful by design: the {@link RunStatsProvider} getters report the last
 * {@link #analyze(String)} call that finished without throwing; a call that throws
 * (e.g. a missing folder) keeps the previous statistics.
 */
public class ProjectAnalyzer implements SourceAnalyzerPort, RunStatsProvider {

    static {
        StaticJavaParser.getParserConfiguration().setLanguageLevel(ParserConfiguration.LanguageLevel.BLEEDING_EDGE);
    }

    // Trazabilidad de la última corrida (ver RunStatsProvider).
    private AnalysisResult lastResult;
    public ProjectAnalyzer() {
    }

    @Override
    public ProjectModel analyze(String folderPath) throws IOException {
        File folder = new File(folderPath);
        if (!folder.isDirectory()) {
            throw new IOException("Folder not found: " + folderPath);
        }
        SourceScanner scanner = new SourceScanner(new TypeExtractor());
        List<ClassModel> classes = new ArrayList<>();
        List<RelationshipModel> relationships = new ArrayList<>();
        scanner.traverseFolder(folder, classes);
        TypeResolver resolver = new TypeResolver(classes, scanner.fileImportsByClass());
        new RelationshipDetector().detectRelationships(classes, relationships, resolver);
        new ExternalTypeRegistrar().registerExternalTypes(classes, relationships, resolver);
        ProjectModel project = withIds(folder.getName(), classes, relationships);
        lastResult = scanner.finish(project);
        return project;
    }

    /**
     * Fixes each class id (its simple name, or its fqn when another class of the project
     * shares the simple name) and turns the relationship endpoints, built as fqn, into ids.
     * Without homonyms every id is the simple name.
     */
    private static ProjectModel withIds(String projectName, List<ClassModel> classes,
                                        List<RelationshipModel> relationships) {
        Map<String, Set<String>> fqnsByName = new HashMap<>();
        for (ClassModel model : classes) {
            fqnsByName.computeIfAbsent(model.getName(), k -> new HashSet<>()).add(model.getFqn());
        }
        Map<String, String> idByFqn = new HashMap<>();
        List<ClassModel> identified = new ArrayList<>();
        for (ClassModel model : classes) {
            String id = fqnsByName.get(model.getName()).size() > 1 ? model.getFqn() : model.getName();
            idByFqn.put(model.getFqn(), id);
            identified.add(model.withId(id));
        }
        List<RelationshipModel> translated = new ArrayList<>();
        for (RelationshipModel rel : relationships) {
            translated.add(new RelationshipModel(idByFqn.getOrDefault(rel.getSource(), rel.getSource()),
                    idByFqn.getOrDefault(rel.getTarget(), rel.getTarget()), rel.getType()));
        }
        return new ProjectModel(projectName, identified, translated);
    }

    /** Files successfully parsed in the last {@link #analyze(String)} run. */
    public List<String> getParsedFiles() {
        return lastResult == null ? new ArrayList<>() : new ArrayList<>(lastResult.parsedFiles());
    }

    /** Files that failed to parse in the last {@link #analyze(String)} run. */
    public List<String> getFailedFiles() {
        return lastResult == null ? new ArrayList<>() : new ArrayList<>(lastResult.failedFiles());
    }

    /** Human-readable "file -> reason" entries for the last run. */
    public List<String> getFailureReasons() {
        return lastResult == null ? new ArrayList<>() : new ArrayList<>(lastResult.failureReasons());
    }

    public int getParsedFileCount() {
        return lastResult == null ? 0 : lastResult.parsedCount();
    }

    public int getFailedFileCount() {
        return lastResult == null ? 0 : lastResult.failedCount();
    }

    public int getTotalJavaFileCount() {
        return lastResult == null ? 0 : lastResult.totalCount();
    }
}
