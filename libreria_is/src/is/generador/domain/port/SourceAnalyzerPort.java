package is.generador.domain.port;

import is.generador.domain.model.ProjectModel;

import java.io.IOException;

/**
 * Inbound port: analyzes Java sources and builds the canonical domain model.
 * Implemented by infrastructure (JavaParser). Domain/application never import
 * the implementation, only this port (Dependency Inversion).
 */
public interface SourceAnalyzerPort {

    ProjectModel analyze(String folderPath) throws IOException;
}
