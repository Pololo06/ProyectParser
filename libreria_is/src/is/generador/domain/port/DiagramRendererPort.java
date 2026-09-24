package is.generador.domain.port;

import is.generador.domain.model.ProjectModel;

/**
 * Outbound port: renders the canonical domain model as diagram source text
 * (e.g. PlantUML). Implemented by infrastructure.
 */
public interface DiagramRendererPort {

    String render(ProjectModel project, boolean groupByPackage);

    default String render(ProjectModel project) {
        return render(project, true);
    }
}
