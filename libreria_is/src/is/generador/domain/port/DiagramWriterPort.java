package is.generador.domain.port;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Outbound port: persists generated diagram text to a file.
 * Implemented by infrastructure.
 */
public interface DiagramWriterPort {

    Path write(Path target, String content) throws IOException;
}
