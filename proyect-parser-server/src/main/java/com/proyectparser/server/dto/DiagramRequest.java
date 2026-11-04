package com.proyectparser.server.dto;

/**
 * Optional settings sent as the {@code request} part of a multipart call.
 */
public record DiagramRequest(
        String projectName,
        FilterRequest filter,
        OptionsRequest options
) {

    public static DiagramRequest empty() {
        return new DiagramRequest(null, null, null);
    }
}
