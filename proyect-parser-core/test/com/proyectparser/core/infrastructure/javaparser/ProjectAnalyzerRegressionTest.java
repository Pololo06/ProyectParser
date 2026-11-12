package com.proyectparser.core.infrastructure.javaparser;

import com.proyectparser.core.domain.model.ProjectModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

class ProjectAnalyzerRegressionTest {
    @TempDir Path root;

    private void source(String path, String content) throws Exception {
        Path file = root.resolve(path);
        Files.createDirectories(file.getParent());
        Files.writeString(file, content);
    }

    private static boolean relation(ProjectModel model, String from, String to, String type) {
        return model.getRelationships().stream().anyMatch(r -> r.getSource().equals(from)
                && r.getTarget().equals(to) && r.getType().equals(type));
    }

    @Test void qualifiedExternalDoesNotResolveToUnrelatedInternal() throws Exception {
        source("Local.java", "package local; class Date {} class Sample { java.sql.Date date; }");
        ProjectModel model = new ProjectAnalyzer().analyze(root.toString());
        assertFalse(relation(model, "Sample", "Date", "ASSOCIATION"));
        assertTrue(model.getClasses().stream().anyMatch(c -> c.isExternal()
                && c.getFqn().equals("java.sql.Date")));
    }

    @Test void qualifiedExternalKeepsItsRealPackageWithoutInternalNamesake() throws Exception {
        source("Sample.java", "class Sample { third.party.Client client; }");
        ProjectModel model = new ProjectAnalyzer().analyze(root.toString());
        assertTrue(model.getClasses().stream().anyMatch(c -> c.isExternal()
                && c.getFqn().equals("third.party.Client")));
    }

    @Test void wildcardImportResolvesInternalHomonyms() throws Exception {
        source("a/Foo.java", "package a; public class Foo {}");
        source("b/Foo.java", "package b; public class Foo {}");
        source("Sample.java", "package use; import b.*; class Sample { Foo foo; }");
        ProjectModel model = new ProjectAnalyzer().analyze(root.toString());
        assertTrue(relation(model, "Sample", "b.Foo", "ASSOCIATION"));
        assertFalse(relation(model, "Sample", "a.Foo", "ASSOCIATION"));
    }

    @Test void javaLangTypeWinsOverUnrelatedWildcardImport() throws Exception {
        source("Sample.java", "import java.util.*; class Sample { Thread thread; }");
        ProjectModel model = new ProjectAnalyzer().analyze(root.toString());
        assertTrue(model.getClasses().stream().anyMatch(c -> c.isExternal()
                && c.getFqn().equals("java.lang.Thread")));
        assertFalse(model.getClasses().stream().anyMatch(c -> c.isExternal()
                && c.getFqn().equals("java.util.Thread")));
    }

    @Test void wildcardImportKeepsNonJdkExternalPackage() throws Exception {
        source("Sample.java", "import third.party.*; class Sample { Client client; }");
        ProjectModel model = new ProjectAnalyzer().analyze(root.toString());
        assertTrue(model.getClasses().stream().anyMatch(c -> c.isExternal()
                && c.getFqn().equals("third.party.Client")));
    }

    @Test void constructorExternalParametersAreIncluded() throws Exception {
        source("Sample.java", "import java.time.Instant; class Sample { Sample(Instant time) {} }");
        ProjectModel model = new ProjectAnalyzer().analyze(root.toString());
        assertTrue(relation(model, "Sample", "Instant", "DEPENDENCY"));
    }

    @Test void annotationMembersAndCompactRecordConstructorsArePreserved() throws Exception {
        source("Types.java", "@interface Label { String value(); } record Item(String name) { public Item { } }");
        ProjectModel model = new ProjectAnalyzer().analyze(root.toString());
        var label = model.getClasses().stream().filter(c -> c.getName().equals("Label")).findFirst().orElseThrow();
        assertEquals("value", label.getMethods().get(0).getName());
        var item = model.getClasses().stream().filter(c -> c.getName().equals("Item")).findFirst().orElseThrow();
        assertEquals(1, item.getConstructors().size());
        assertEquals("String", item.getConstructors().get(0).getParameters().get(0).getType());
    }

    @Test void parserSupportsRecordsOnEveryRequestThread() throws Exception {
        source("Item.java", "record Item(String value) {}");
        new ProjectAnalyzer();
        int failures = CompletableFuture.supplyAsync(() -> {
            try {
                ProjectAnalyzer analyzer = new ProjectAnalyzer();
                analyzer.analyze(root.toString());
                return analyzer.getFailedFileCount();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).get();
        assertEquals(0, failures);
    }

    @Test void scanIgnoresSymbolicLinksOutsideSourceTree() throws Exception {
        source("project/A.java", "class A {}");
        source("outside/Secret.java", "class Secret {}");
        Files.createSymbolicLink(root.resolve("project/link"), root.resolve("outside"));
        ProjectAnalyzer analyzer = new ProjectAnalyzer();
        ProjectModel model = analyzer.analyze(root.resolve("project").toString());
        assertEquals(1, analyzer.getParsedFileCount());
        assertEquals("A", model.getClasses().get(0).getName());
    }

    @Test void parseFailureIsReportedWithoutDiscardingValidSources() throws Exception {
        source("A.java", "class A {}");
        source("Broken.java", "class Broken { ??? }");
        ProjectAnalyzer analyzer = new ProjectAnalyzer();
        ProjectModel model = analyzer.analyze(root.toString());
        assertEquals(1, analyzer.getParsedFileCount());
        assertEquals(1, analyzer.getFailedFileCount());
        assertEquals(2, analyzer.getTotalJavaFileCount());
        assertEquals(1, model.getClasses().size());
    }
}
