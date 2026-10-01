package com.proyectparser.core;

import com.proyectparser.core.application.DiagramService;
import com.proyectparser.core.domain.port.DiagramRendererPort;
import com.proyectparser.core.domain.port.DiagramWriterPort;
import com.proyectparser.core.domain.port.SourceAnalyzerPort;
import com.proyectparser.core.infrastructure.javaparser.ProjectAnalyzer;
import com.proyectparser.core.infrastructure.plantuml.PlantUmlGenerator;
import com.proyectparser.core.infrastructure.plantuml.PumlFileWriter;

/**
 * Composition root: the only place that chooses the production adapters
 * (JavaParser, PlantUML, files). Consumers get ready-made use cases without
 * naming infrastructure.
 */
public final class UseCases {

    private UseCases() {
    }

    /** The use case wired with the production adapters. */
    public static DiagramService diagramas() {
        return new DiagramService(analizador(), renderizador(), escritor());
    }

    static SourceAnalyzerPort analizador() {
        return new ProjectAnalyzer();
    }

    static DiagramRendererPort renderizador() {
        return new PlantUmlGenerator();
    }

    static DiagramWriterPort escritor() {
        return PumlFileWriter::write;
    }
}
