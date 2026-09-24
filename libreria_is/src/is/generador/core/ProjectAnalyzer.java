package is.generador.core;

import is.generador.domain.policy.DiagramFilter;

/**
 * @deprecated use {@link is.generador.infrastructure.javaparser.ProjectAnalyzer} instead.
 * Compatibility shim; will be removed in v2.
 */
@Deprecated
public class ProjectAnalyzer extends is.generador.infrastructure.javaparser.ProjectAnalyzer {

    public ProjectAnalyzer() {
        super();
    }

    public ProjectAnalyzer(DiagramFilter filter) {
        super(filter);
    }
}
