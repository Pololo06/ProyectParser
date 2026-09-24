package is.generador.infra;

import java.io.IOException;
import java.nio.file.Path;

/**
 * @deprecated use {@link is.generador.infrastructure.plantuml.PumlFileWriter} instead.
 * Compatibility shim; will be removed in v2.
 */
@Deprecated
public final class PumlFileWriter {

    private PumlFileWriter() {
    }

    public static Path write(Path target, String content) throws IOException {
        return is.generador.infrastructure.plantuml.PumlFileWriter.write(target, content);
    }
}
