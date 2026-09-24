package is.generador.application;

import is.generador.domain.model.ProjectModel;
import is.generador.domain.port.RunStatsProvider;
import is.generador.domain.port.SourceAnalyzerPort;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * Use case: analyze a source tree. Depends only on the
 * {@link SourceAnalyzerPort}, never on JavaParser directly.
 */
public class AnalyzeProjectUseCase {

    private final SourceAnalyzerPort analyzer;

    public AnalyzeProjectUseCase(SourceAnalyzerPort analyzer) {
        this.analyzer = Objects.requireNonNull(analyzer, "analyzer is required");
    }

    public ProjectModel execute(String folderPath) throws IOException {
        return analyzer.analyze(folderPath);
    }

    /** Full run with traceability, when the analyzer exposes run stats. */
    public AnalysisResult executeWithStats(String folderPath) throws IOException {
        ProjectModel project = analyzer.analyze(folderPath);
        if (analyzer instanceof RunStatsProvider stats) {
            return new AnalysisResult(project,
                    stats.getParsedFiles(), stats.getFailedFiles(), stats.getFailureReasons());
        }
        return new AnalysisResult(project, List.of(), List.of(), List.of());
    }
}
