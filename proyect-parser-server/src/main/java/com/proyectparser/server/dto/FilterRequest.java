package com.proyectparser.server.dto;

import java.util.List;

/**
 * Blacklist/whitelist for the diagram, mirroring the core
 * {@code DiagramFilter} (blacklist &gt; whitelist &gt; allow).
 */
public record FilterRequest(
        List<String> excludePackages,
        List<String> excludeClasses,
        List<String> includePackages,
        List<String> includeClasses
) {
}
