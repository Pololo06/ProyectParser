package is.generador.application;

import is.generador.domain.model.ProjectModel;
import is.generador.domain.policy.DiagramFilter;
import is.generador.domain.policy.DiagramOptions;
import is.generador.domain.policy.ProjectFilter;
import is.generador.domain.port.DiagramRendererPort;
import is.generador.domain.port.DiagramWriterPort;
import is.generador.domain.port.SourceAnalyzerPort;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Use case: analyze, (optionally filter,) render and persist a diagram.
 * Orchestrates {@link SourceAnalyzerPort} + {@link ProjectFilter} +
 * {@link DiagramRendererPort} + {@link DiagramWriterPort}; owns no parsing,
 * rendering or I/O logic itself.
 *
 * <p>{@link #generate} and {@link #export} run the whole flow in one call.
 * {@link #analyze}, {@link #render} and {@link #write} expose the steps for
 * callers that review an already analyzed model between them.</p>
 */
public class DiagramService {

    private final SourceAnalyzerPort analyzer;
    private final DiagramRendererPort renderer;
    private final DiagramWriterPort writer;

    public DiagramService(SourceAnalyzerPort analyzer, DiagramRendererPort renderer, DiagramWriterPort writer) {
        this.analyzer = Objects.requireNonNull(analyzer, "analyzer is required");
        this.renderer = Objects.requireNonNull(renderer, "renderer is required");
        this.writer = Objects.requireNonNull(writer, "writer is required");
    }

    public ProjectModel analyze(String folderPath) throws IOException {
        return analyzer.analyze(folderPath);
    }

    /** Renders an already analyzed (and possibly filtered) model; {@code null} options mean defaults. */
    public String render(ProjectModel project, DiagramOptions options) {
        return renderer.render(project, options == null ? DiagramOptions.defaults() : options);
    }

    public Path write(Path target, String content) throws IOException {
        return writer.write(target, content);
    }

    public String generate(String folderPath, DiagramFilter filter, DiagramOptions options) throws IOException {
        ProjectModel project = analyzer.analyze(folderPath);
        ProjectModel filtered = ProjectFilter.apply(project, filter == null ? new DiagramFilter() : filter);
        return render(filtered, options);
    }

    public Path export(String folderPath, Path outputFile, DiagramFilter filter, DiagramOptions options)
            throws IOException {
        return write(outputFile, generate(folderPath, filter, options));
    }
}
