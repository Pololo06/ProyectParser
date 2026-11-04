package com.proyectparser.server.service;

import com.proyectparser.core.domain.model.ProjectModel;

import java.util.List;

/**
 * Raw result of one analysis. File counters come from the core run; the model
 * is already filtered so rendering uses exactly the same snapshot.
 */
public record AnalysisOutcome(
        ProjectModel project,
        int totalJavaFiles,
        int parsedJavaFiles,
        int failedJavaFiles,
        List<String> failedFiles,
        List<String> failureReasons
) {
}
