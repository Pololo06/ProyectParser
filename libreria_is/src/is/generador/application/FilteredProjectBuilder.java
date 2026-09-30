package is.generador.application;

import is.generador.domain.model.ProjectModel;
import is.generador.domain.model.RelationshipModel;
import is.generador.domain.policy.DiagramFilter;
import is.generador.domain.policy.ProjectFilter;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Temporal: solo conserva la API antigua para que {@code AppGenerador} compile sin
 * cambios. Delega en {@link ProjectFilter}; se borra en la fase 6 del plan.
 */
public class FilteredProjectBuilder {

    private ProjectModel originalProject;
    private final Set<String> vetoedRelationships = new HashSet<>();
    private DiagramFilter filter;

    public FilteredProjectBuilder() {
    }

    public FilteredProjectBuilder(ProjectModel originalProject) {
        this.originalProject = originalProject;
    }

    public FilteredProjectBuilder setFilter(DiagramFilter filter) {
        this.filter = filter;
        return this;
    }

    public FilteredProjectBuilder withFilter(DiagramFilter filter) {
        return setFilter(filter);
    }

    public static String relationshipKey(String source, String type, String target) {
        return new RelationshipModel(source, target, type).key();
    }

    public static String relationshipKey(RelationshipModel rel) {
        return rel.key();
    }

    public FilteredProjectBuilder excludeRelationships(Collection<String> relationshipKeys) {
        if (relationshipKeys != null) {
            for (String key : relationshipKeys) {
                if (key != null && !key.trim().isEmpty()) {
                    this.vetoedRelationships.add(key.trim());
                }
            }
        }
        return this;
    }

    public ProjectModel build() {
        return ProjectFilter.apply(originalProject, filter, vetoedRelationships);
    }
}
