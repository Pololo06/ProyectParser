package is.generador.application;

import is.generador.domain.model.ProjectModel;
import is.generador.domain.port.SourceAnalyzerPort;

import java.io.IOException;
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
}
