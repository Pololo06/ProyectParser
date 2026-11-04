package com.proyectparser.server.dto;

/**
 * Counters of the last analysis. File counters always refer to the uploaded
 * source tree; class/relationship/package counters refer to the returned
 * (already filtered) model and exclude external {@code @external} boxes.
 */
public record StatisticsDto(
        int totalJavaFiles,
        int parsedJavaFiles,
        int failedJavaFiles,
        int classes,
        int externalClasses,
        int relationships,
        int packages
) {
}
