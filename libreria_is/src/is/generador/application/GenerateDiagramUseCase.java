package is.generador.application;

import is.generador.domain.model.ProjectModel;
import is.generador.domain.policy.DiagramFilter;
import is.generador.domain.port.DiagramRendererPort;
import is.generador.domain.port.SourceAnalyzerPort;

import java.io.IOException;
import java.util.Objects;

/**
 * Use case: analyze (optionally filtered) and render a diagram.
 * Orchestrates {@link SourceAnalyzerPort} + {@link FilteredProjectBuilder} +
 * {@link DiagramRendererPort}; owns no parsing or rendering logic itself.
 */
public class GenerateDiagramUseCase {

    private final SourceAnalyzerPort analyzer;
    private final DiagramRendererPort renderer;

    public GenerateDiagramUseCase(SourceAnalyzerPort analyzer, DiagramRendererPort renderer) {
        this.analyzer = Objects.requireNonNull(analyzer, "analyzer is required");
        this.renderer = Objects.requireNonNull(renderer, "renderer is required");
    }

    public String execute(String folderPath) throws IOException {
        return renderer.render(analyzer.analyze(folderPath));
    }

    public String execute(String folderPath, DiagramFilter filter) throws IOException {
        ProjectModel project = analyzer.analyze(folderPath);
        ProjectModel filtered = new FilteredProjectBuilder(project).withFilter(filter).build();
        return renderer.render(filtered);
    }

    public String execute(String folderPath, DiagramFilter filter, boolean groupByPackage) throws IOException {
        ProjectModel project = analyzer.analyze(folderPath);
        ProjectModel filtered = new FilteredProjectBuilder(project).withFilter(filter).build();
        return renderer.render(filtered, groupByPackage);
    }
}
