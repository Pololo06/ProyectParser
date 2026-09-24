package is.generador.infrastructure.plantuml;

import is.generador.domain.port.DiagramWriterPort;

import java.io.IOException;
import java.nio.file.Path;

/**
 * File-system adapter for {@link DiagramWriterPort}.
 * Delegates to the {@link PumlFileWriter} static utility so application code
 * depends on the port, never on a static call.
 */
public class FileSystemDiagramWriter implements DiagramWriterPort {

    @Override
    public Path write(Path target, String content) throws IOException {
        return PumlFileWriter.write(target, content);
    }
}
