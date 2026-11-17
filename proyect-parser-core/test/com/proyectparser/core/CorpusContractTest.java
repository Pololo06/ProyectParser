package com.proyectparser.core;

import com.proyectparser.core.domain.policy.DiagramFilter;
import com.proyectparser.core.domain.policy.DiagramOptions;
import com.proyectparser.core.infrastructure.plantuml.PlantUmlGenerator;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;
import static org.junit.jupiter.api.Assertions.*;

/** Public API checks independent of the PlantUML reference comparator. */
class CorpusContractTest {
    @TempDir Path output;

    @ParameterizedTest(name = "corpus {0}: source, model, filter, render, export")
    @ValueSource(ints = {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25})
    void publicPipelinePreservesModelIntegrity(int number) throws Exception {
        Path source = Path.of("..", "casos-generador", "caso_%02d".formatted(number), "src", "org", "evilmind");
        var facade = new DiagramFacade();
        var model = facade.analyzeProject(source.toString());
        try (var files = Files.walk(source)) {
            assertEquals(files.filter(p -> p.toString().endsWith(".java")).count(), facade.getParsedFileCount());
        }
        assertEquals(0, facade.getErrorCount());
        var ids = model.getClasses().stream().map(c -> c.id()).collect(Collectors.toSet());
        assertEquals(model.getClasses().size(), ids.size(), "Type identities must be unique");
        var keys = model.getRelationships().stream().map(r -> r.key()).collect(Collectors.toSet());
        assertEquals(model.getRelationships().size(), keys.size(), "Relations must be unique");
        for (var relation : model.getRelationships()) {
            assertTrue(ids.contains(relation.getSource()), relation.key());
            assertTrue(ids.contains(relation.getTarget()), relation.key());
        }
        var filter = new DiagramFilter.Builder().excludeClass("DiagramaClases").build();
        var options = DiagramOptions.defaults();
        String text = facade.generatePlantUml(source.toString(), filter, options);
        assertEquals(text, facade.generatePlantUml(source.toString(), filter, options), "Deterministic generation");
        Path written = facade.exportPlantUml(source.toString(), output.resolve("diagram"), filter, options);
        assertTrue(written.toString().endsWith(".puml"));
        assertEquals(text, Files.readString(written));
        String hidden = new PlantUmlGenerator().render(model,
                new DiagramOptions.Builder().showExternal(false).build());
        assertFalse(hidden.lines().anyMatch(line -> line.strip().equals("<<external>>")));
        assertFalse(hidden.contains("EXTERNAL RELATIONSHIPS"));
    }
}
