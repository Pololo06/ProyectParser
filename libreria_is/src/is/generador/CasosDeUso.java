package is.generador;

import is.generador.application.DiagramService;
import is.generador.domain.port.DiagramRendererPort;
import is.generador.domain.port.DiagramWriterPort;
import is.generador.domain.port.SourceAnalyzerPort;
import is.generador.infrastructure.javaparser.ProjectAnalyzer;
import is.generador.infrastructure.plantuml.PlantUmlGenerator;
import is.generador.infrastructure.plantuml.PumlFileWriter;

/**
 * Composition root: the only place that chooses the production adapters
 * (JavaParser, PlantUML, files). Consumers get ready-made use cases without
 * naming infrastructure.
 */
public final class CasosDeUso {

    private CasosDeUso() {
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
