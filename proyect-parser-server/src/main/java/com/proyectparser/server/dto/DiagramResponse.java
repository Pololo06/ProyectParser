package com.proyectparser.server.dto;

import java.util.List;

/** Result of the diagram endpoints: PlantUML text plus traceability data. */
public record DiagramResponse(
        String projectName,
        String fileName,
        String plantUml,
        StatisticsDto statistics,
        List<String> failedFiles,
        List<String> failureReasons
) {
}
