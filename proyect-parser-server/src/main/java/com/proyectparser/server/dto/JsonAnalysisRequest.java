package com.proyectparser.server.dto;

import java.util.List;

/**
 * Body of the JSON endpoints: the same settings as {@link DiagramRequest} plus
 * the source files. This is the recommended format for application-to-application
 * integration because it avoids building a ZIP archive.
 */
public record JsonAnalysisRequest(
        String projectName,
        List<SourceFileRequest> files,
        FilterRequest filter,
        OptionsRequest options
) {

    public DiagramRequest toDiagramRequest() {
        return new DiagramRequest(projectName, filter, options);
    }
}
