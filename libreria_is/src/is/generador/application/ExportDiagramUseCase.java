package is.generador.application;

import is.generador.domain.policy.DiagramFilter;
import is.generador.domain.port.DiagramWriterPort;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Use case: generate a diagram and persist it. Depends on the
 * {@link GenerateDiagramUseCase} and the {@link DiagramWriterPort}.
 */
public class ExportDiagramUseCase {

    private final GenerateDiagramUseCase generator;
    private final DiagramWriterPort writer;

    public ExportDiagramUseCase(GenerateDiagramUseCase generator, DiagramWriterPort writer) {
        this.generator = Objects.requireNonNull(generator, "generator is required");
        this.writer = Objects.requireNonNull(writer, "writer is required");
    }

    public Path execute(String folderPath, Path outputFile) throws IOException {
        return writer.write(outputFile, generator.execute(folderPath));
    }

    public Path execute(String folderPath, Path outputFile, DiagramFilter filter) throws IOException {
        return writer.write(outputFile, generator.execute(folderPath, filter));
    }
}
