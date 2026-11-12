package com.universidad.tools;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CliProcessTest {

    @TempDir
    Path tmp;

    private void source(String path, String content) throws Exception {
        Path file = tmp.resolve(path);
        Files.createDirectories(file.getParent());
        Files.writeString(file, content);
    }

    private static void assertContains(String text, String expected) {
        assertTrue(text.contains(expected), () -> "No se encontró \"" + expected + "\" en:\n" + text);
    }

    @Test
    void helpWinsWithoutReadingStdin() throws Exception {
        CliProcess.Result result = CliProcess.run(tmp, "", "--help");
        assertEquals(0, result.exitCode(), result.stderr());
        assertContains(result.stdout(), "Uso: AppGenerador");
        assertFalse(result.stdout().contains("> BLACKLIST"));
    }

    @Test
    void invalidExplicitSourceFailsInsteadOfAnalyzingAnotherFolder() throws Exception {
        source("src/A.java", "class A {}");
        CliProcess.Result result = CliProcess.run(tmp, "", "no-existe");
        assertNotEquals(0, result.exitCode());
        assertContains(result.stderr(), "no existe");
        assertFalse(result.stdout().contains("Ruta fuente detectada automáticamente"),
                "no debe caer al src del directorio actual");
        assertFalse(Files.exists(tmp.resolve("diagrama_filtrado.puml")));
    }

    @Test
    void emptySourceFolderFails() throws Exception {
        Files.createDirectories(tmp.resolve("src"));
        CliProcess.Result result = CliProcess.run(tmp, "", "src");
        assertNotEquals(0, result.exitCode());
        assertContains(result.stderr(), "No se encontraron archivos .java");
    }

    @Test
    void analysisFailureReturnsNonZeroAndKeepsNoMisleadingDiagram() throws Exception {
        source("src/Broken.java", "class Broken { ??? }");
        CliProcess.Result result = CliProcess.run(tmp, "", "src");
        assertNotEquals(0, result.exitCode());
        assertContains(result.stderr(), "parcial");
        assertContains(result.stderr(), "No hay fuentes válidas");
        assertFalse(Files.exists(tmp.resolve("diagrama_filtrado.puml")));
    }

    @Test
    void partialAnalysisWarnsNamesTheFileAndReturnsNonZero() throws Exception {
        source("src/A.java", "class A {}");
        source("src/Broken.java", "class Broken { ??? }");
        CliProcess.Result result = CliProcess.run(tmp, "", "src", "parcial.puml", "--sin-dependencias");
        assertEquals(1, result.exitCode(), result.stderr());
        assertContains(result.stderr(), "parcial");
        assertContains(result.stderr(), "Broken.java");
        assertContains(Files.readString(tmp.resolve("parcial.puml")), "class A");
    }

    @Test
    void writeFailureReturnsNonZero() throws Exception {
        source("src/A.java", "class A {}");
        Files.writeString(tmp.resolve("bloqueo"), "archivo, no carpeta");
        CliProcess.Result result = CliProcess.run(tmp, "", "src", "bloqueo/salida.puml");
        assertNotEquals(0, result.exitCode());
        assertContains(result.stderr(), "No se pudo escribir");
    }

    @Test
    void reportedPathIncludesTheEffectivePumlExtension() throws Exception {
        source("src/A.java", "class A {}");
        CliProcess.Result result = CliProcess.run(tmp, "", "src", "salida");
        assertEquals(0, result.exitCode(), result.stderr());
        assertContains(result.stdout(), "salida.puml");
        assertTrue(Files.exists(tmp.resolve("salida.puml")));
    }

    @Test
    void defaultSelectionsGenerateTheDiagramWithEmptyStdin() throws Exception {
        source("src/Curso.java", "package p; public class Curso { java.time.Instant inicio; }");
        CliProcess.Result result = CliProcess.run(tmp, "", "src", "real.puml");
        assertEquals(0, result.exitCode(), result.stderr());
        String puml = Files.readString(tmp.resolve("real.puml"));
        assertContains(puml, "class Curso");
    }

    @Test
    void summaryExternalFlatAndModuleFlagsAreApplied() throws Exception {
        source("src/Curso.java", "package p; public class Curso { java.time.Instant inicio; }");
        CliProcess.Result result = CliProcess.run(tmp, "", "src", "flags.puml",
                "--resumen", "--external", "--flat", "--modulo", "Curso");
        assertEquals(0, result.exitCode(), result.stderr());
        String puml = Files.readString(tmp.resolve("flags.puml"));
        assertContains(puml, "hide members");
        assertContains(puml, "EXTERNAL");
        assertContains(puml, "Instant");
        assertFalse(puml.contains("package p {"));
    }

    @Test
    void homonymSelectionUsesItsFqnAndLeavesTheOtherOut() throws Exception {
        source("src/a/Foo.java", "package a; public class Foo {}");
        source("src/b/Foo.java", "package b; public class Foo {}");
        CliProcess.Result result = CliProcess.run(tmp, "\n\n\na.Foo\n\n", "src", "homonimos.puml");
        assertEquals(0, result.exitCode(), result.stderr());
        assertContains(result.stdout(), "a.Foo");
        assertContains(result.stdout(), "b.Foo");
        String puml = Files.readString(tmp.resolve("homonimos.puml"));
        assertContains(puml, "a_Foo");
        assertFalse(puml.contains("b_Foo"), puml);
    }

    @Test
    void emptyFlagValueIsAUsageError() throws Exception {
        source("src/A.java", "class A {}");
        CliProcess.Result result = CliProcess.run(tmp, "", "src", "--modulo=");
        assertEquals(2, result.exitCode(), result.stderr());
        assertContains(result.stderr(), "Falta el nombre del módulo");
    }

    @Test
    void misspelledModuleFlagIsUnknown() throws Exception {
        source("src/A.java", "class A {}");
        CliProcess.Result result = CliProcess.run(tmp, "", "src", "--modulox");
        assertEquals(2, result.exitCode(), result.stderr());
        assertContains(result.stderr(), "Flag desconocido");
    }

    @Test
    void misspelledExcludeFlagIsUnknown() throws Exception {
        source("src/A.java", "class A {}");
        CliProcess.Result result = CliProcess.run(tmp, "", "src", "--excluirx");
        assertEquals(2, result.exitCode(), result.stderr());
        assertContains(result.stderr(), "Flag desconocido");
    }

    @Test
    void unknownModuleFailsWithoutWritingTheWholeProject() throws Exception {
        source("src/A.java", "class A {}");
        CliProcess.Result result = CliProcess.run(tmp, "", "src", "modulo.puml",
                "--modulo", "ModuloQueNoExiste");
        assertEquals(2, result.exitCode(), result.stderr());
        assertContains(result.stderr(), "Ninguna clase coincide con el módulo");
        assertFalse(Files.exists(tmp.resolve("modulo.puml")));
    }

    @Test
    void repeatedExcludeFlagsAreKept() throws Exception {
        source("src/a/A.java", "package a; public class A {}");
        source("src/b/B.java", "package b; public class B {}");
        source("src/c/C.java", "package c; public class C {}");
        CliProcess.Result result = CliProcess.run(tmp, "", "src", "excluidos.puml",
                "--excluir=a", "--excluir=b");
        assertEquals(0, result.exitCode(), result.stderr());
        String puml = Files.readString(tmp.resolve("excluidos.puml"));
        assertFalse(puml.contains("class A"), puml);
        assertFalse(puml.contains("class B"), puml);
        assertContains(puml, "class C");
    }

    @Test
    void equalsModuleSyntaxStillWorks() throws Exception {
        source("src/Curso.java", "package p; public class Curso {}");
        CliProcess.Result result = CliProcess.run(tmp, "", "src", "modulo-eq.puml", "--modulo=Curso");
        assertEquals(0, result.exitCode(), result.stderr());
        assertContains(Files.readString(tmp.resolve("modulo-eq.puml")), "class Curso");
    }

    @Test
    void unknownFlagIsAUsageError() throws Exception {
        source("src/A.java", "class A {}");
        CliProcess.Result result = CliProcess.run(tmp, "", "src", "--flag-inexistente");
        assertEquals(2, result.exitCode(), result.stderr());
        assertContains(result.stderr(), "Flag desconocido");
    }
}
