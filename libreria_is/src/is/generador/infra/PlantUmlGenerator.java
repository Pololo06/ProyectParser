package is.generador.infra;

import is.generador.core.model.AttributeModel;
import is.generador.core.model.ClassModel;
import is.generador.core.model.ConstructorModel;
import is.generador.core.model.MethodModel;
import is.generador.core.model.ParameterModel;
import is.generador.core.model.ProjectModel;
import is.generador.core.model.RelationshipModel;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Renders the internal domain model as PlantUML class diagram source.
 *
 * <p>Project classes are grouped by their Java package into PlantUML
 * {@code package "..." { }} blocks. External classes (stereotype
 * {@code @external}) are always rendered apart, in their own
 * {@code package "EXTERNAL" { }} block at the end, so both worlds never mix.</p>
 */
public class PlantUmlGenerator {

    /** true si la clase es externa (estereotipo {@code @external}). */
    public static boolean isExternal(ClassModel model) {
        return model != null && model.getStereotypes() != null
                && model.getStereotypes().contains("@external");
    }

    /** Renders the project grouped by package (default behavior). */
    public String generate(ProjectModel project) {
        return generate(project, true);
    }

    /** Renders the project flat, without package blocks (legacy behavior). */
    public String generateFlat(ProjectModel project) {
        return generate(project, false);
    }

    public String generate(ProjectModel project, boolean groupByPackage) {
        StringBuilder builder = new StringBuilder();
        builder.append("@startuml\n");
        builder.append("skinparam classAttributeIconSize 0\n\n");

        List<ClassModel> internals = new ArrayList<>();
        List<ClassModel> externals = new ArrayList<>();
        if (project.getClasses() != null) {
            for (ClassModel model : project.getClasses()) {
                (isExternal(model) ? externals : internals).add(model);
            }
        }
        externals.sort(Comparator.comparing(ClassModel::getName));

        if (groupByPackage) {
            Map<String, List<ClassModel>> byPackage = new TreeMap<>();
            for (ClassModel model : internals) {
                String pkg = model.getPackageName() == null ? "" : model.getPackageName();
                byPackage.computeIfAbsent(pkg, k -> new ArrayList<>()).add(model);
            }
            for (List<ClassModel> models : byPackage.values()) {
                models.sort(Comparator.comparing(ClassModel::getName));
            }
            for (Map.Entry<String, List<ClassModel>> entry : byPackage.entrySet()) {
                builder.append("package \"").append(packageLabel(entry.getKey())).append("\" {\n");
                for (ClassModel model : entry.getValue()) {
                    appendClass(builder, model, "  ");
                }
                builder.append("}\n\n");
            }
            if (!externals.isEmpty()) {
                builder.append("package \"EXTERNAL\" {\n");
                for (ClassModel model : externals) {
                    appendClass(builder, model, "  ");
                }
                builder.append("}\n\n");
            }
        } else {
            for (ClassModel model : internals) {
                appendClass(builder, model, "");
            }
            if (!externals.isEmpty()) {
                builder.append("' ---------------- EXTERNAL ----------------\n");
                for (ClassModel model : externals) {
                    appendClass(builder, model, "");
                }
            }
        }

        appendRelationships(builder, project);
        builder.append("\n@enduml\n");
        return builder.toString();
    }

    /** Groups classes by package name, sorted alphabetically (packages and classes). */
    public Map<String, List<ClassModel>> groupByPackage(ProjectModel project) {
        Map<String, List<ClassModel>> byPackage = new TreeMap<>();
        for (ClassModel model : project.getClasses()) {
            String pkg = model.getPackageName() == null ? "" : model.getPackageName();
            byPackage.computeIfAbsent(pkg, k -> new ArrayList<>()).add(model);
        }
        for (List<ClassModel> models : byPackage.values()) {
            models.sort(Comparator.comparing(ClassModel::getName));
        }
        return byPackage;
    }

    private String packageLabel(String packageName) {
        return (packageName == null || packageName.isEmpty()) ? "(default package)" : packageName;
    }

    private void appendClass(StringBuilder builder, ClassModel model, String indent) {
        builder.append(indent)
                .append(keywordFor(model)).append(" ").append(model.getName()).append(" {\n");

        for (String stereotype : model.getStereotypes()) {
            builder.append(indent).append("  <<").append(stereotype.replace("@", "")).append(">>\n");
        }

        // enum literals first, as plain constants
        if ("Enum".equals(model.getKind()) && model.getEnumConstants() != null) {
            for (String constant : model.getEnumConstants()) {
                builder.append(indent).append("  ").append(constant).append("\n");
            }
        }

        for (AttributeModel attribute : model.getAttributes()) {
            builder.append(indent).append("  ")
                    .append(visibilityOf(attribute.getModifiers()))
                    .append(attribute.getType())
                    .append(" ")
                    .append(attribute.getName())
                    .append(modifierSuffix(attribute.getModifiers()))
                    .append("\n");
        }

        for (ConstructorModel constructor : model.getConstructors()) {
            builder.append(indent).append("  ")
                    .append(visibilityOf(constructor.getModifiers()))
                    .append(constructor.getName())
                    .append("(")
                    .append(formatParameters(constructor.getParameters()))
                    .append(")")
                    .append(modifierSuffix(constructor.getModifiers()))
                    .append("\n");
        }

        for (MethodModel method : model.getMethods()) {
            builder.append(indent).append("  ")
                    .append(visibilityOf(method.getModifiers()))
                    .append(method.getReturnType())
                    .append(" ")
                    .append(method.getName())
                    .append("(")
                    .append(formatParameters(method.getParameters()))
                    .append(")")
                    .append(modifierSuffix(method.getModifiers()))
                    .append("\n");
        }

        builder.append(indent).append("}\n\n");
    }

    private void appendRelationships(StringBuilder builder, ProjectModel project) {
        if (project.getRelationships() == null) {
            return;
        }
        Set<String> externalNames = new HashSet<>();
        if (project.getClasses() != null) {
            for (ClassModel model : project.getClasses()) {
                if (isExternal(model)) {
                    externalNames.add(model.getName());
                }
            }
        }
        List<RelationshipModel> internalRels = new ArrayList<>();
        List<RelationshipModel> externalRels = new ArrayList<>();
        for (RelationshipModel relationship : project.getRelationships()) {
            if (externalNames.contains(relationship.getSource())
                    || externalNames.contains(relationship.getTarget())) {
                externalRels.add(relationship);
            } else {
                internalRels.add(relationship);
            }
        }
        for (RelationshipModel relationship : internalRels) {
            appendRelationship(builder, relationship);
        }
        if (!externalRels.isEmpty()) {
            builder.append("' ---------------- EXTERNAL RELATIONSHIPS ----------------\n");
            for (RelationshipModel relationship : externalRels) {
                appendRelationship(builder, relationship);
            }
        }
    }

    private void appendRelationship(StringBuilder builder, RelationshipModel relationship) {
        // In PlantUML the triangle head points at the parent:
        // "Parent <|-- Child", "Interface <|.. Implementation".
        // Our model stores source=child, target=parent, so swap them here.
        String type = relationship.getType();
        if ("EXTENDS".equals(type) || "IMPLEMENTS".equals(type)) {
                builder.append(relationship.getTarget())
                        .append(" ")
                        .append(arrowFor(type))
                        .append(" ")
                        .append(relationship.getSource())
                        .append("\n");
            } else {
                builder.append(relationship.getSource())
                        .append(" ")
                        .append(arrowFor(type))
                        .append(" ")
                        .append(relationship.getTarget())
                        .append("\n");
            }
    }

    private String keywordFor(ClassModel model) {
        switch (model.getKind()) {
            case "Interface":
                return "interface";
            case "Enum":
                return "enum";
            case "Annotation":
                return "annotation";
            case "Record":
                return "record";
            default:
                if (model.isAbstract()) {
                    return "abstract class";
                }
                return "class";
        }
    }

    private String arrowFor(String relationshipType) {
        if ("EXTENDS".equals(relationshipType)) {
            return "<|--";
        }
        if ("IMPLEMENTS".equals(relationshipType)) {
            return "<|..";
        }
        if ("DEPENDENCY".equals(relationshipType)) {
            return "..>";
        }
        return "-->";
    }

    private String formatParameters(List<ParameterModel> parameters) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < parameters.size(); i++) {
            ParameterModel parameter = parameters.get(i);
            builder.append(parameter.getType()).append(" ").append(parameter.getName());
            if (i < parameters.size() - 1) {
                builder.append(", ");
            }
        }
        return builder.toString();
    }

    private String visibilityOf(List<String> modifiers) {
        if (modifiers == null) {
            return "~";
        }
        if (modifiers.contains("public")) {
            return "+";
        }
        if (modifiers.contains("private")) {
            return "-";
        }
        if (modifiers.contains("protected")) {
            return "#";
        }
        return "~";
    }

    /**
     * PlantUML suffixes for non-visibility modifiers.
     * e.g. " {static}", " {abstract}". Final is intentionally not rendered:
     * PlantUML has no standard {final} marker and it would only add noise.
     */
    private String modifierSuffix(List<String> modifiers) {
        if (modifiers == null || modifiers.isEmpty()) {
            return "";
        }
        StringBuilder suffix = new StringBuilder();
        if (modifiers.contains("static")) {
            suffix.append(" {static}");
        }
        if (modifiers.contains("abstract")) {
            suffix.append(" {abstract}");
        }
        return suffix.toString();
    }
}