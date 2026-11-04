package com.proyectparser.server.dto;

import java.time.Instant;

/** Liveness/readiness response used by Railway's healthcheck. */
public record HealthResponse(
        String status,
        String service,
        String version,
        Instant timestamp
) {
}
