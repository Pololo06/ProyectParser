package com.proyectparser.server.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Configuration of the public API.
 *
 * <p>Every property can be overridden with an environment variable, which is
 * how the service is configured on Railway (for example
 * {@code PROYECTPARSER_API_MAX_FILES=2000}).</p>
 */
@ConfigurationProperties(prefix = "proyectparser.api")
public class ApiProperties {

    /** Maximum number of {@code .java} files accepted in one request. */
    private int maxFiles = 5_000;

    /** Maximum size of a single uncompressed {@code .java} file, in bytes. */
    private long maxFileBytes = 2L * 1024 * 1024;

    /** Maximum size of all uncompressed {@code .java} files in one request, in bytes. */
    private long maxTotalBytes = 32L * 1024 * 1024;

    /** Directory where temporary source workspaces are created. Empty = JVM temp directory. */
    private Path tempDir;

    /** Allowed CORS origin patterns. {@code "*"} allows every origin. */
    private List<String> allowedOrigins = new ArrayList<>(List.of("*"));

    /** Service version reported by the health and info endpoints. */
    private String version = "1.0.0";

    /** Public base path of the API. */
    private String basePath = "/api/v1";

    public int getMaxFiles() {
        return maxFiles;
    }

    public void setMaxFiles(int maxFiles) {
        this.maxFiles = maxFiles;
    }

    public long getMaxFileBytes() {
        return maxFileBytes;
    }

    public void setMaxFileBytes(long maxFileBytes) {
        this.maxFileBytes = maxFileBytes;
    }

    public long getMaxTotalBytes() {
        return maxTotalBytes;
    }

    public void setMaxTotalBytes(long maxTotalBytes) {
        this.maxTotalBytes = maxTotalBytes;
    }

    public Path getTempDir() {
        return tempDir;
    }

    public void setTempDir(Path tempDir) {
        this.tempDir = tempDir;
    }

    public List<String> getAllowedOrigins() {
        return allowedOrigins;
    }

    public void setAllowedOrigins(List<String> allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }
}
