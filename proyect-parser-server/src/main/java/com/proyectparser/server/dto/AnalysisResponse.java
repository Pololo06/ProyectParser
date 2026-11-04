package com.proyectparser.server.dto;

import java.util.List;

/**
 * Full result of {@code POST /analysis}. Externals are returned separately
 * from source classes to keep the two concepts apart.
 */
public record AnalysisResponse(
        String projectName,
        StatisticsDto statistics,
        java.util.Map<String, Integer> kinds,
        List<PackageDto> packages,
        List<ClassDto> classes,
        List<RelationshipDto> relationships,
        java.util.Map<String, String> externalClasses,
        List<String> failedFiles,
        List<String> failureReasons
) {
}
