package is.generador.application;

import is.generador.domain.model.ProjectModel;

import java.util.List;

/**
 * Stateless result of an analysis run. Replaces the stateful getters on the
 * old analyzer ({@code getParsedFiles/getFailedFiles/...}): all traceability
 * data travels with the result instead of living in mutable fields.
 */
public record AnalysisResult(
        ProjectModel project,
        List<String> parsedFiles,
        List<String> failedFiles,
        List<String> failureReasons) {

    public int parsedCount() {
        return parsedFiles == null ? 0 : parsedFiles.size();
    }

    public int failedCount() {
        return failedFiles == null ? 0 : failedFiles.size();
    }

    public int totalCount() {
        return parsedCount() + failedCount();
    }

    public String summary() {
        return "parsed " + parsedCount() + "/" + totalCount() + ", failed " + failedCount();
    }
}
