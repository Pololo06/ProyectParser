package com.proyectparser.server.service;

/** Analysis plus the PlantUML text rendered from it. */
public record DiagramOutcome(AnalysisOutcome analysis, String plantUml) {
}
