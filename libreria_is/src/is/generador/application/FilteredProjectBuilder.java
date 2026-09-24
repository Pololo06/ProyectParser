package is.generador.application;

import is.generador.domain.model.ClassModel;
import is.generador.domain.model.PackageModel;
import is.generador.domain.model.ProjectModel;
import is.generador.domain.model.RelationshipModel;

import is.generador.domain.policy.DiagramFilter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FilteredProjectBuilder {

    private ProjectModel originalProject;
    private final Set<String> excludedPackages = new HashSet<>();
    private final Set<String> excludedClasses = new HashSet<>();
    private DiagramFilter filter;

    public FilteredProjectBuilder() {
    }

    public FilteredProjectBuilder(ProjectModel originalProject) {
        this.originalProject = originalProject;
    }

    /** Asocia un {@link DiagramFilter} (blacklist/whitelist); null lo limpia. */
    public FilteredProjectBuilder setFilter(DiagramFilter filter) {
        this.filter = filter;
        return this;
    }

    /** Alias fluido de {@link #setFilter(DiagramFilter)}. */
    public FilteredProjectBuilder withFilter(DiagramFilter filter) {
        return setFilter(filter);
    }

    public FilteredProjectBuilder setProject(ProjectModel originalProject) {
        this.originalProject = originalProject;
        return this;
    }

    public FilteredProjectBuilder excludePackage(String packageName) {
        if (packageName != null && !packageName.trim().isEmpty()) {
            this.excludedPackages.add(packageName.trim());
        }
        return this;
    }

    public FilteredProjectBuilder excludePackages(Collection<String> packageNames) {
        if (packageNames != null) {
            packageNames.forEach(this::excludePackage);
        }
        return this;
    }

    public FilteredProjectBuilder excludeClass(String className) {
        if (className != null && !className.trim().isEmpty()) {
            this.excludedClasses.add(className.trim());
        }
        return this;
    }

    public FilteredProjectBuilder excludeClasses(Collection<String> classNames) {
        if (classNames != null) {
            classNames.forEach(this::excludeClass);
        }
        return this;
    }

    public ProjectModel build() {
        if (originalProject == null) {
            throw new IllegalStateException("Se requiere un ProjectModel original para construir el modelo filtrado.");
        }

        // 1. Consolidar en O(1) todas las clases vetadas directas y por paquete
        Set<String> blacklistedClasses = new HashSet<>(this.excludedClasses);

        if (originalProject.getPackages() != null) {
            for (PackageModel pkg : originalProject.getPackages()) {
                if (this.excludedPackages.contains(pkg.getPackageName())) {
                    if (pkg.getTypeNames() != null) {
                        blacklistedClasses.addAll(pkg.getTypeNames());
                    }
                }
            }
        }

        if (originalProject.getClasses() != null) {
            for (ClassModel clazz : originalProject.getClasses()) {
                if (this.excludedPackages.contains(clazz.getPackageName())) {
                    blacklistedClasses.add(clazz.getName());
                }
            }
        }

        // 1b. Fusionar DiagramFilter (blacklist veta; whitelist no vacía = restrictivo)
        if (originalProject.getClasses() != null && this.filter != null) {
            for (ClassModel clazz : originalProject.getClasses()) {
                if (!this.filter.isAllowed(clazz.getPackageName(), clazz.getName())) {
                    blacklistedClasses.add(clazz.getName());
                }
            }
        }

        // 2. Filtrar clases sobrevivientes
        List<ClassModel> filteredClasses = new ArrayList<>();
        if (originalProject.getClasses() != null) {
            for (ClassModel clazz : originalProject.getClasses()) {
                if (!blacklistedClasses.contains(clazz.getName())) {
                    filteredClasses.add(clazz);
                }
            }
        }

        // 3. Filtrar relaciones comprobando que ambos extremos sobrevivan
        List<RelationshipModel> filteredRelationships = new ArrayList<>();
        if (originalProject.getRelationships() != null) {
            for (RelationshipModel rel : originalProject.getRelationships()) {
                if (!blacklistedClasses.contains(rel.getSource()) && !blacklistedClasses.contains(rel.getTarget())) {
                    filteredRelationships.add(rel);
                }
            }
        }

        // 4. Instanciar el ProjectModel canónico con getProjectName()
        return new ProjectModel(originalProject.getProjectName(), filteredClasses, filteredRelationships);
    }
}
