package is.generador.infra;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** File adapter (Fase 2/7): writes generated PlantUML text to a .puml file. */
public final class PumlFileWriter {

    private PumlFileWriter() {
    }

    /**
     * Writes {@code content} to {@code target} (creating parent dirs) and returns {@code target}.
     * Forces the {@code .puml} extension when missing.
     */
    public static Path write(Path target, String content) throws IOException {
        Path resolved = target;
        if (!target.getFileName().toString().endsWith(".puml")) {
            resolved = target.resolveSibling(target.getFileName() + ".puml");
        }
        if (resolved.getParent() != null) {
            Files.createDirectories(resolved.getParent());
        }
        Files.writeString(resolved, content, StandardCharsets.UTF_8);
        return resolved;
    }
}
