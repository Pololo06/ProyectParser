package is.generador.infrastructure.plantuml;

import is.generador.domain.BeanAccessors;
import is.generador.domain.model.AttributeModel;
import is.generador.domain.model.ClassModel;
import is.generador.domain.model.ConstructorModel;
import is.generador.domain.model.Kind;
import is.generador.domain.model.MethodModel;
import is.generador.domain.model.ParameterModel;
import is.generador.domain.model.ProjectModel;
import is.generador.domain.model.RelType;
import is.generador.domain.model.RelationshipModel;
import is.generador.domain.policy.DiagramOptions;
import is.generador.domain.port.DiagramRendererPort;

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
public class PlantUmlGenerator implements DiagramRendererPort {

    /** true si la clase es externa (estereotipo {@code @external}). */
    public static boolean isExternal(ClassModel model) {
        return model != null && model.isExternal();
    }

    /** Renders the project grouped by package (default behavior). */
    public String generate(ProjectModel project) {
        return generate(project, true);
    }

    /** Renders the project flat, without package blocks (legacy behavior). */
    public String generateFlat(ProjectModel project) {
        return generate(project, false);
    }

    @Override
    public String render(ProjectModel project, boolean groupByPackage) {
        return generate(project, groupByPackage);
    }

    @Override
    public String render(ProjectModel project, DiagramOptions options) {
        return generate(project, options);
    }

    public String generate(ProjectModel project, boolean groupByPackage) {
        return generate(project, new DiagramOptions.Builder().groupByPackage(groupByPackage).build());
    }

    /** Renders the project applying the given {@link DiagramOptions}. */
    public String generate(ProjectModel project, DiagramOptions options) {
        DiagramOptions opt = options == null ? DiagramOptions.defaults() : options;
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
        if (!opt.isShowExternal()) {
            externals.clear();
        } else if (!opt.isShowJdkTypes()) {
            externals.removeIf(model -> isJdkPackage(model.getPackageName()));
        }
        externals.sort(Comparator.comparing(ClassModel::getName));

        if (opt.isGroupByPackage()) {
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
                    appendClass(builder, model, "  ", opt);
                }
                builder.append("}\n\n");
            }
            if (!externals.isEmpty()) {
                builder.append("package \"EXTERNAL\" {\n");
                for (ClassModel model : externals) {
                    appendClass(builder, model, "  ", opt);
                }
                builder.append("}\n\n");
            }
        } else {
            for (ClassModel model : internals) {
                appendClass(builder, model, "", opt);
            }
            if (!externals.isEmpty()) {
                builder.append("' ---------------- EXTERNAL ----------------\n");
                for (ClassModel model : externals) {
                    appendClass(builder, model, "", opt);
                }
            }
        }

        appendRelationships(builder, project, renderedNames(internals, externals));
        builder.append("\n@enduml\n");
        return builder.toString();
    }

    /** Names of the rendered (non-hidden) classes. */
    private static Set<String> renderedNames(List<ClassModel> internals, List<ClassModel> externals) {
        Set<String> names = new HashSet<>();
        if (internals != null) {
            for (ClassModel model : internals) {
                names.add(model.getName());
            }
        }
        if (externals != null) {
            for (ClassModel model : externals) {
                names.add(model.getName());
            }
        }
        return names;
    }

    /** true for JDK packages ({@code java.*}), kept apart from third-party externals. */
    static boolean isJdkPackage(String packageName) {
        return packageName != null
                && (packageName.equals("java.lang") || packageName.startsWith("java."));
    }

    /** Groups classes by package name, sorted alphabetically (packages and classes). Null-safe. */
    public Map<String, List<ClassModel>> groupByPackage(ProjectModel project) {
        Map<String, List<ClassModel>> byPackage = new TreeMap<>();
        if (project == null || project.getClasses() == null) {
            return byPackage;
        }
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

    private void appendClass(StringBuilder builder, ClassModel model, String indent, DiagramOptions opt) {
        builder.append(indent)
                .append(keywordFor(model)).append(" ").append(model.getName()).append(" {\n");

        if (model.getStereotypes() != null) {
            for (String stereotype : model.getStereotypes()) {
                builder.append(indent).append("  <<").append(stereotype.replace("@", "")).append(">>\n");
            }
        }

        // enum literals first, as plain constants
        if (model.getKindEnum() == Kind.ENUM && model.getEnumConstants() != null) {
            for (String constant : model.getEnumConstants()) {
                builder.append(indent).append("  ").append(constant).append("\n");
            }
        }

        if (opt.isShowAttributes() && model.getAttributes() != null) {
            for (AttributeModel attribute : model.getAttributes()) {
                builder.append(indent).append("  ")
                        .append(visibilityOf(attribute.getModifiers()))
                        .append(attribute.getType())
                        .append(" ")
                        .append(attribute.getName())
                        .append(modifierSuffix(attribute.getModifiers()))
                        .append("\n");
            }
        }

        if (opt.isShowConstructors() && model.getConstructors() != null) {
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
        }

        if (opt.isShowMethods() && model.getMethods() != null) {
            for (MethodModel method : model.getMethods()) {
                if (!opt.isShowGettersSetters()
                        && (BeanAccessors.isGetter(method, model) || BeanAccessors.isSetter(method, model))) {
                    continue;
                }
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
        }

        builder.append(indent).append("}\n\n");
    }

    private void appendRelationships(StringBuilder builder, ProjectModel project, Set<String> renderedNames) {
        if (project.getRelationships() == null) {
            return;
        }
        Set<String> externalNames = new HashSet<>();
        if (project.getClasses() != null) {
            for (ClassModel model : project.getClasses()) {
                if (isExternal(model) && renderedNames.contains(model.getName())) {
                    externalNames.add(model.getName());
                }
            }
        }
        List<RelationshipModel> internalRels = new ArrayList<>();
        List<RelationshipModel> externalRels = new ArrayList<>();
        for (RelationshipModel relationship : project.getRelationships()) {
            // Drop relations pointing at hidden (filtered-out) boxes.
            if (!renderedNames.contains(relationship.getSource())
                    || !renderedNames.contains(relationship.getTarget())) {
                continue;
            }
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
        Kind kind = model == null ? Kind.CLASS : model.getKindEnum();
        switch (kind) {
            case INTERFACE:
                return "interface";
            case ENUM:
                return "enum";
            case ANNOTATION:
                return "annotation";
            case RECORD:
                return "record";
            default:
                if (model != null && model.isAbstract()) {
                    return "abstract class";
                }
                return "class";
        }
    }

    static String arrowFor(RelType relationshipType) {
        if (relationshipType == null) {
            return "-->";
        }
        switch (relationshipType) {
            case EXTENDS:
                return "<|--";
            case IMPLEMENTS:
                return "<|..";
            case DEPENDENCY:
                return "..>";
            default:
                return "-->";
        }
    }

    private String arrowFor(String relationshipType) {
        return arrowFor(RelType.fromLabel(relationshipType));
    }

    private String formatParameters(List<ParameterModel> parameters) {
        if (parameters == null || parameters.isEmpty()) {
            return "";
        }
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