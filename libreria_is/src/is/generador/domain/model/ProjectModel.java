package is.generador.domain.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** Canonical project model (Fase 1). No JavaParser/PlantUML dependencies. */
public class ProjectModel {
    private String projectName;
    private List<ClassModel> classes;
    private List<RelationshipModel> relationships;

    public ProjectModel(String projectName, List<ClassModel> classes, List<RelationshipModel> relationships) {
        this.projectName = projectName;
        this.classes = classes;
        this.relationships = relationships;
    }

    public String getProjectName() { return projectName; }
    public List<ClassModel> getClasses() { return classes; }
    public List<RelationshipModel> getRelationships() { return relationships; }

    /** Derived package views, sorted by package name (Fase 10). Null-safe. */
    public List<PackageModel> getPackages() {
        Map<String, List<String>> byPackage = new TreeMap<>();
        if (classes == null) {
            return List.of();
        }
        for (ClassModel model : classes) {
            String pkg = model.getPackageName() == null ? "" : model.getPackageName();
            byPackage.computeIfAbsent(pkg, k -> new ArrayList<>()).add(model.getName());
        }
        List<PackageModel> result = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : byPackage.entrySet()) {
            result.add(new PackageModel(entry.getKey(), entry.getValue()));
        }
        return result;
    }
}
