package com.proyectparser.server.service;

import com.proyectparser.server.config.ApiProperties;
import com.proyectparser.server.dto.SourceFileRequest;
import com.proyectparser.server.exception.InvalidSourceException;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Isolated, disposable source tree for one request.
 *
 * <p>All uploaded content is materialized below a temporary directory because
 * {@code proyect-parser-core} analyzes folders. Only regular {@code .java}
 * files are written: entries with other extensions are ignored, and archive
 * paths are normalized to prevent path traversal ("zip slip"). The directory is
 * removed by {@link #close()}.</p>
 *
 * <p>Layout:</p>
 * <pre>
 * &lt;temp&gt;/proyectparser-XXXX/&lt;projectName&gt;/json/...
 * &lt;temp&gt;/proyectparser-XXXX/&lt;projectName&gt;/upload/...
 * &lt;temp&gt;/proyectparser-XXXX/&lt;projectName&gt;/zip/...
 * </pre>
 * The analyzed folder is {@code <projectName>}, so the library reports exactly
 * the requested project name.
 */
public final class SourceWorkspace implements AutoCloseable {

    private static final int BUFFER_SIZE = 8 * 1024;

    private final ApiProperties properties;
    private final Path workspaceRoot;
    private final Path projectRoot;
    private final Set<String> writtenFiles = new HashSet<>();

    private int fileCount;
    private long totalBytes;

    private SourceWorkspace(ApiProperties properties, Path workspaceRoot, Path projectRoot) {
        this.properties = properties;
        this.workspaceRoot = workspaceRoot;
        this.projectRoot = projectRoot;
    }

    /**
     * Creates an empty workspace. {@code requestedName} is sanitized to a safe
     * folder name; it becomes the {@code projectName} of the response.
     */
    public static SourceWorkspace create(ApiProperties properties, String requestedName) throws IOException {
        Path tempRoot = properties.getTempDir() == null
                ? Path.of(System.getProperty("java.io.tmpdir"))
                : properties.getTempDir();
        Files.createDirectories(tempRoot);

        Path workspaceRoot = Files.createTempDirectory(tempRoot, "proyectparser-");
        String projectName = sanitizeProjectName(requestedName);
        Path projectRoot = workspaceRoot.resolve(projectName);
        Files.createDirectories(projectRoot);
        return new SourceWorkspace(properties, workspaceRoot, projectRoot);
    }

    /** Folder analyzed by the core library; its name is the response project name. */
    public Path sourceRoot() {
        return projectRoot;
    }

    public String projectName() {
        return projectRoot.getFileName().toString();
    }

    public boolean isEmpty() {
        return fileCount == 0;
    }

    public void requireSources() {
        if (isEmpty()) {
            throw new InvalidSourceException(
                    "At least one .java source file is required (ZIP archive, 'files' parts or JSON 'files')");
        }
    }

    /**
     * Adds one multipart upload. A {@code .zip} archive is expanded; a
     * {@code .java} file is stored directly.
     */
    public void addMultipartFile(MultipartFile upload) throws IOException {
        if (upload == null || upload.isEmpty()) {
            return;
        }
        String original = upload.getOriginalFilename();
        String filename = cleanFilename(original);
        if (filename.toLowerCase(Locale.ROOT).endsWith(".zip")) {
            addZip(upload, filename);
        } else if (filename.endsWith(".java")) {
            addJavaUpload(upload, filename);
        } else {
            throw new InvalidSourceException(
                    "Unsupported file '" + filename + "'. Upload a .zip archive or .java files");
        }
    }

    /** Adds one file from a JSON request. */
    public void addSourceFile(SourceFileRequest source) throws IOException {
        if (source == null) {
            throw new InvalidSourceException("Every entry of 'files' must be an object with 'path' and 'content'");
        }
        Path relative = requireJavaPath(source.path(), "files[].path");
        byte[] bytes = (source.content() == null ? "" : source.content()).getBytes(StandardCharsets.UTF_8);
        Path target = projectRoot.resolve("json").resolve(relative).normalize();
        writeBytes(target, relative.toString(), bytes);
    }

    private void addJavaUpload(MultipartFile upload, String filename) throws IOException {
        String safeName = cleanFilename(filename);
        if (!safeName.endsWith(".java")) {
            throw new InvalidSourceException("File '" + filename + "' is not a .java file");
        }
        Path target = uniqueUploadPath(safeName);
        checkCanAdd(upload.getSize(), safeName);
        Path destination = projectRoot.resolve("upload").resolve(target).normalize();
        ensureInsideProject(destination, safeName);
        ensureNotDuplicate(destination, safeName);
        Files.createDirectories(destination.getParent());

        long written;
        try (InputStream input = upload.getInputStream()) {
            written = copy(input, destination, safeName);
        }
        committed(written);
    }

    private void addZip(MultipartFile upload, String filename) throws IOException {
        try (ZipInputStream zip = new ZipInputStream(
                new BufferedInputStream(upload.getInputStream(), BUFFER_SIZE))) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }
                String entryName = entry.getName();
                if (entryName == null || !entryName.endsWith(".java")) {
                    continue;
                }
                Path relative = requireJavaPath(entryName, "ZIP entry");
                Path destination = projectRoot.resolve("zip").resolve(relative).normalize();
                ensureInsideProject(destination, entryName);
                ensureNotDuplicate(destination, entryName);
                checkCanAdd(entry.getSize(), entryName);
                Files.createDirectories(destination.getParent());

                long written = copy(zip, destination, entryName);
                committed(written);
            }
        } catch (InvalidSourceException e) {
            throw e;
        } catch (IOException e) {
            throw new InvalidSourceException("Invalid or unreadable ZIP archive '" + filename + "': "
                    + e.getMessage(), e);
        }
    }

    private void writeBytes(Path destination, String displayName, byte[] bytes) throws IOException {
        ensureInsideProject(destination, displayName);
        ensureNotDuplicate(destination, displayName);
        checkCanAdd(bytes.length, displayName);
        Files.createDirectories(destination.getParent());
        Files.write(destination, bytes);
        committed(bytes.length);
    }

    private long copy(InputStream input, Path destination, String displayName) throws IOException {
        long written = 0;
        byte[] buffer = new byte[BUFFER_SIZE];
        try (OutputStream out = Files.newOutputStream(destination)) {
            int read;
            while ((read = input.read(buffer)) != -1) {
                written += read;
                if (written > properties.getMaxFileBytes()) {
                    throw new InvalidSourceException("File '" + displayName + "' exceeds the limit of "
                            + properties.getMaxFileBytes() + " bytes");
                }
                if (totalBytes + written > properties.getMaxTotalBytes()) {
                    throw new InvalidSourceException("The uploaded sources exceed the total limit of "
                            + properties.getMaxTotalBytes() + " bytes");
                }
                out.write(buffer, 0, read);
            }
        }
        return written;
    }

    private void checkCanAdd(long size, String displayName) {
        if (fileCount >= properties.getMaxFiles()) {
            throw new InvalidSourceException("Too many .java files; the limit is "
                    + properties.getMaxFiles());
        }
        if (size > properties.getMaxFileBytes()) {
            throw new InvalidSourceException("File '" + displayName + "' exceeds the limit of "
                    + properties.getMaxFileBytes() + " bytes");
        }
        if (size >= 0 && totalBytes + size > properties.getMaxTotalBytes()) {
            throw new InvalidSourceException("The uploaded sources exceed the total limit of "
                    + properties.getMaxTotalBytes() + " bytes");
        }
    }

    private void committed(long bytes) {
        fileCount++;
        totalBytes += bytes;
    }

    private void ensureNotDuplicate(Path destination, String displayName) {
        String key = destination.toAbsolutePath().normalize().toString();
        if (!writtenFiles.add(key)) {
            throw new InvalidSourceException("Duplicate source path: " + displayName);
        }
    }

    private void ensureInsideProject(Path destination, String displayName) {
        Path base = projectRoot.toAbsolutePath().normalize();
        Path normalized = destination.toAbsolutePath().normalize();
        if (!normalized.startsWith(base)) {
            throw new InvalidSourceException("Invalid path outside the source tree: " + displayName);
        }
    }

    private Path uniqueUploadPath(String filename) {
        Path uploadDir = projectRoot.resolve("upload");
        Path candidate = uploadDir.resolve(filename);
        if (!Files.exists(candidate)) {
            return Path.of(filename);
        }
        int dot = filename.lastIndexOf('.');
        String stem = dot > 0 ? filename.substring(0, dot) : filename;
        String extension = dot > 0 ? filename.substring(dot) : "";
        for (int suffix = 2; suffix < 10_000; suffix++) {
            Path next = Path.of(stem + "-" + suffix + extension);
            if (!Files.exists(uploadDir.resolve(next))) {
                return next;
            }
        }
        throw new InvalidSourceException("Too many uploads with the same file name: " + filename);
    }

    private static Path requireJavaPath(String rawPath, String location) {
        if (rawPath == null || rawPath.isBlank()) {
            throw new InvalidSourceException(location + " must not be blank");
        }
        String normalized = rawPath.trim().replace('\\', '/');
        if (normalized.startsWith("/") || normalized.matches("^[A-Za-z]:.*")) {
            throw new InvalidSourceException(location + " must be a relative path: " + rawPath);
        }
        try {
            Path path = Path.of(normalized).normalize();
            if (path.isAbsolute() || path.getNameCount() == 0 || path.toString().isBlank()) {
                throw new InvalidSourceException(location + " must be a relative file path: " + rawPath);
            }
            for (Path part : path) {
                if ("..".equals(part.toString())) {
                    throw new InvalidSourceException(location + " must not contain '..': " + rawPath);
                }
            }
            if (!path.getFileName().toString().endsWith(".java")) {
                throw new InvalidSourceException(location + " must point to a .java file: " + rawPath);
            }
            return path;
        } catch (InvalidPathException e) {
            throw new InvalidSourceException("Invalid path in " + location + ": " + rawPath, e);
        }
    }

    private static String cleanFilename(String originalFilename) {
        String value = originalFilename == null ? "" : originalFilename.trim().replace('\\', '/');
        int slash = value.lastIndexOf('/');
        if (slash >= 0) {
            value = value.substring(slash + 1);
        }
        if (value.isBlank() || ".".equals(value) || "..".equals(value)) {
            throw new InvalidSourceException("Every uploaded file needs a valid file name");
        }
        return value;
    }

    /** Safe folder name used as the project name. */
    public static String sanitizeProjectName(String requestedName) {
        if (requestedName == null || requestedName.isBlank()) {
            return "project";
        }
        String value = requestedName.trim().replace('\\', '/');
        int slash = value.lastIndexOf('/');
        if (slash >= 0) {
            value = value.substring(slash + 1);
        }
        value = value.replaceAll("[^A-Za-z0-9._-]", "_");
        value = value.replaceAll("^\\.+", "").replaceAll("\\.+$", "");
        if (value.isBlank() || ".".equals(value) || "..".equals(value)) {
            return "project";
        }
        if (value.matches("(?i)CON|PRN|AUX|NUL|COM[1-9]|LPT[1-9]")) {
            value = "project-" + value;
        }
        return value.length() > 80 ? value.substring(0, 80) : value;
    }

    @Override
    public void close() {
        try {
            if (Files.exists(workspaceRoot)) {
                try (var paths = Files.walk(workspaceRoot)) {
                    paths.sorted(Comparator.reverseOrder()).forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException ignored) {
                            // Best-effort cleanup: the OS temp cleaner is the fallback.
                        }
                    });
                }
            }
        } catch (IOException ignored) {
            // Best-effort cleanup.
        }
    }
}
