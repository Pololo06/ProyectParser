package is.generador.core;

import is.generador.domain.model.ProjectModel;

/**
 * @deprecated use {@link is.generador.application.FilteredProjectBuilder} instead.
 * Compatibility shim; will be removed in v2.
 */
@Deprecated
public class FilteredProjectBuilder extends is.generador.application.FilteredProjectBuilder {

    public FilteredProjectBuilder() {
        super();
    }

    public FilteredProjectBuilder(ProjectModel originalProject) {
        super(originalProject);
    }
}
