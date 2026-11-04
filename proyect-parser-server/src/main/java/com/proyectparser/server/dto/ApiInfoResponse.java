package com.proyectparser.server.dto;

import java.util.List;

/** Machine-readable index of the API, served at the base path. */
public record ApiInfoResponse(
        String service,
        String version,
        String description,
        List<EndpointInfo> endpoints,
        LimitsInfo limits
) {

    public record EndpointInfo(String method, String path, String contentType, String description) {
    }

    public record LimitsInfo(long maxFiles, long maxFileBytes, long maxTotalBytes) {
    }
}
