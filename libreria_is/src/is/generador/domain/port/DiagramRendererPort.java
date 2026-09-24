package is.generador.domain.port;

import is.generador.domain.model.ProjectModel;
import is.generador.domain.policy.DiagramOptions;

/**
 * Outbound port: renders the canonical domain model as diagram source text
 * (e.g. PlantUML). Implemented by infrastructure.
 */
public interface DiagramRendererPort {

    String render(ProjectModel project, boolean groupByPackage);

    default String render(ProjectModel project) {
        return render(project, true);
    }

    /** Renders applying display options; default honors only grouping. */
    default String render(ProjectModel project, DiagramOptions options) {
        DiagramOptions opt = options == null ? DiagramOptions.defaults() : options;
        return render(project, opt.isGroupByPackage());
    }
}
