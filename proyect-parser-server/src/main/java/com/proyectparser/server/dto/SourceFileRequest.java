package com.proyectparser.server.dto;

/**
 * One Java source file sent as JSON: {@code path} is a relative path used only
 * for traceability (it must end in {@code .java}); {@code content} is the
 * complete source code.
 */
public record SourceFileRequest(String path, String content) {
}
