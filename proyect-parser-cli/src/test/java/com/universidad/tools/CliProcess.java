package com.universidad.tools;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/** Runs the CLI in a real child JVM, with the current test classpath or a built JAR. */
final class CliProcess {

    record Result(int exitCode, String stdout, String stderr) {
    }

    private CliProcess() {
    }

    static Result run(Path cwd, String stdin, String... args) throws IOException, InterruptedException {
        List<String> command = new ArrayList<>();
        command.add(javaBin());
        command.add("-cp");
        command.add(classpath());
        command.add("com.universidad.tools.AppGenerador");
        command.addAll(Arrays.asList(args));
        return start(cwd, command, stdin);
    }

    static Result runJar(Path cwd, Path jar, String stdin, String... args)
            throws IOException, InterruptedException {
        List<String> command = new ArrayList<>();
        command.add(javaBin());
        command.add("-jar");
        command.add(jar.toAbsolutePath().toString());
        command.addAll(Arrays.asList(args));
        return start(cwd, command, stdin);
    }

    private static Result start(Path cwd, List<String> command, String stdin)
            throws IOException, InterruptedException {
        Process process = new ProcessBuilder(command).directory(cwd.toFile()).start();
        CompletableFuture<String> out = CompletableFuture.supplyAsync(() -> read(process.getInputStream()));
        CompletableFuture<String> err = CompletableFuture.supplyAsync(() -> read(process.getErrorStream()));
        try (OutputStream input = process.getOutputStream()) {
            input.write(stdin.getBytes(StandardCharsets.UTF_8));
        }
        int exit = process.waitFor();
        try {
            return new Result(exit, out.get(), err.get());
        } catch (ExecutionException e) {
            throw new IOException("No se pudo leer la salida del proceso", e.getCause());
        }
    }

    private static String read(java.io.InputStream stream) {
        try {
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String javaBin() {
        return Path.of(System.getProperty("java.home"), "bin", "java").toString();
    }

    private static String classpath() {
        String surefire = System.getProperty("surefire.test.class.path");
        return surefire != null && !surefire.isBlank()
                ? surefire : System.getProperty("java.class.path");
    }
}
