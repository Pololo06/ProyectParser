package is.generador.infrastructure.javaparser;

import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.StaticJavaParser;
import is.generador.application.AnalysisResult;
import is.generador.domain.model.ClassModel;
import is.generador.domain.model.ProjectModel;
import is.generador.domain.model.RelationshipModel;

import java.io.File;
import java.io.IOException;
import is.generador.domain.policy.DiagramFilter;
import is.generador.domain.port.RunStatsProvider;
import is.generador.domain.port.SourceAnalyzerPort;

import java.util.ArrayList;
import java.util.List;

/**
 * Analyzes a Java source tree with JavaParser and builds the internal domain model.
 *
 * <p>Thin orchestrator (public API): {@link SourceScanner} walks files,
 * {@link TypeExtractor} builds {@code ClassModel}s, {@link RelationshipDetector}
 * deduces relations and {@link ExternalTypeRegistrar} adds {@code @external} boxes.
 * Covers nested types at any depth, generic arguments and enum constants.
 */
public class ProjectAnalyzer implements SourceAnalyzerPort, RunStatsProvider {

    static {
        StaticJavaParser.getParserConfiguration().setLanguageLevel(ParserConfiguration.LanguageLevel.BLEEDING_EDGE);
    }

    // Trazabilidad de la última corrida (ver RunStatsProvider).
    private AnalysisResult lastResult;
    // Filtro blacklist/whitelist (nunca null: vacío = permisivo).
    private DiagramFilter filter = new DiagramFilter();

    public ProjectAnalyzer() {
    }

    public ProjectAnalyzer(DiagramFilter filter) {
        setFilter(filter);
    }

    public ProjectAnalyzer setFilter(DiagramFilter filter) {
        this.filter = (filter != null) ? filter : new DiagramFilter();
        return this;
    }

    public DiagramFilter getFilter() {
        return filter;
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
        new RelationshipDetector().detectRelationships(classes, relationships);
        new ExternalTypeRegistrar(filter)
                .registerExternalTypes(classes, relationships, scanner.fileImportsByClass());
        ProjectModel project = new ProjectModel(folder.getName(), classes, relationships);
        lastResult = scanner.finish(project);
        return project;
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
