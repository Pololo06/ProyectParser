package com.universidad.tools;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Runs the shaded JAR with `java -jar` from a temporary directory outside the repository. */
class JarExecutionIT {

    @TempDir
    Path tmp;

    private static Path jar() {
        String basedir = System.getProperty("basedir", ".");
        return Path.of(basedir, "target", "proyect-parser-cli-1.0.0.jar");
    }

    @Test
    void helpWorksFromOutsideTheRepository() throws Exception {
        CliProcess.Result result = CliProcess.runJar(tmp, jar(), "", "--help");
        assertEquals(0, result.exitCode(), result.stderr());
        assertTrue(result.stdout().contains("Uso: AppGenerador"), result.stdout());
    }

    @Test
    void analyzesRealSourcesFromOutsideTheRepository() throws Exception {
        Path source = tmp.resolve("src/A.java");
        Files.createDirectories(source.getParent());
        Files.writeString(source, "class A { java.time.Instant when; }");

        CliProcess.Result result = CliProcess.runJar(tmp, jar(), "", "src", "desde-jar");
        assertEquals(0, result.exitCode(), result.stderr());
        Path output = tmp.resolve("desde-jar.puml");
        assertTrue(Files.exists(output), result.stdout());
        assertTrue(Files.readString(output).contains("class A"), result.stdout());
    }
}
