package com.proyectparser.core.infrastructure.plantuml;

import com.proyectparser.core.domain.BeanAccessors;
import com.proyectparser.core.domain.model.AttributeModel;
import com.proyectparser.core.domain.model.ClassModel;
import com.proyectparser.core.domain.model.ConstructorModel;
import com.proyectparser.core.domain.model.Kind;
import com.proyectparser.core.domain.model.MethodModel;
import com.proyectparser.core.domain.model.ParameterModel;
import com.proyectparser.core.domain.model.PackageModel;
import com.proyectparser.core.domain.model.ProjectModel;
import com.proyectparser.core.domain.model.RelType;
import com.proyectparser.core.domain.model.RelationshipModel;
import com.proyectparser.core.domain.policy.DiagramOptions;
import com.proyectparser.core.domain.port.DiagramRendererPort;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

/**
 * Renders the internal domain model as PlantUML class diagram source.
 *
 * <p>Project classes are grouped by their Java package into PlantUML
 * {@code package "..." { }} blocks. External classes (stereotype
 * {@code @external}) are always rendered apart, in their own
 * {@code package "EXTERNAL" { }} block at the end, so both worlds never mix.</p>
 *
 * <p>Classes are identified by {@link ClassModel#id()}. A homonym (id different from its
 * name) is declared as {@code class "Foo" as h_a_Foo}, and its relationships use the alias.</p>
 */
public class PlantUmlGenerator implements DiagramRendererPort {

    /** By name and, for homonyms, by fqn. */
    private static final Comparator<ClassModel> BY_NAME = Comparator.comparing(ClassModel::getName)
            .thenComparing(ClassModel::getFqn);

    @Override
    public String render(ProjectModel project, boolean groupByPackage) {
        return generate(project, groupByPackage);
    }

    @Override
    public String render(ProjectModel project, DiagramOptions options) {
        return generate(project, options);
    }

    private String generate(ProjectModel project, boolean groupByPackage) {
        return generate(project, new DiagramOptions.Builder().groupByPackage(groupByPackage).build());
    }

    /** Renders the project applying the given {@link DiagramOptions}. */
    private String generate(ProjectModel project, DiagramOptions options) {
        DiagramOptions opt = options == null ? DiagramOptions.defaults() : options;
        StringBuilder builder = new StringBuilder();
        builder.append("@startuml\n");
        builder.append("skinparam classAttributeIconSize 0\n");
        if (opt.getLineType() != DiagramOptions.LineType.DEFAULT) {
            builder.append("skinparam linetype ").append(opt.getLineType().name().toLowerCase()).append("\n");
        }
        // Layout: more space between nodes keeps the relationship lines apart.
        builder.append("skinparam nodesep 80\n");
        builder.append("skinparam ranksep 100\n");
        // Flat package boxes: com.x.y stays one box instead of nested com > x > y.
        builder.append("set separator none\n");
        if (opt.isSummary()) {
            // Overview: class boxes only; members stay in the source for other views.
            builder.append("hide members\n");
        }
        builder.append("\n");

        List<ClassModel> internals = new ArrayList<>();
        List<ClassModel> externals = new ArrayList<>();
        if (project.getClasses() != null) {
            for (ClassModel model : project.getClasses()) {
                (model.isExternal() ? externals : internals).add(model);
            }
        }
        if (!opt.isShowExternal()) {
            externals.clear();
        } else if (!opt.isShowJdkTypes()) {
            externals.removeIf(model -> isJdkPackage(model.getPackageName()));
        }
        externals.sort(BY_NAME);
        if (opt.isHideOrphans()) {
            // Orphans: no visible relationship after options/filters. Dropping one never
            // hides another relationship, so a single pass is enough.
            Set<String> connected = new HashSet<>();
            for (RelationshipModel relationship
                    : visibleRelationships(project, renderedNames(internals, externals), opt)) {
                connected.add(relationship.getSource());
                connected.add(relationship.getTarget());
            }
            internals.removeIf(model -> !connected.contains(model.id()));
            externals.removeIf(model -> !connected.contains(model.id()));
        }

        if (opt.isGroupByPackage()) {
            Map<String, List<ClassModel>> byPackage = new TreeMap<>();
            for (ClassModel model : internals) {
                String pkg = model.getPackageName() == null ? "" : model.getPackageName();
                byPackage.computeIfAbsent(pkg, k -> new ArrayList<>()).add(model);
            }
            for (List<ClassModel> models : byPackage.values()) {
                models.sort(BY_NAME);
            }
            // Packages of each layer, by rank; packages outside the layer map are unranked.
            Map<Integer, List<String>> packagesByRank = new TreeMap<>();
            List<String> unranked = new ArrayList<>();
            for (String pkg : byPackage.keySet()) {
                Integer rank = opt.layerRankOf(pkg);
                if (rank == null) {
                    unranked.add(pkg);
                } else {
                    packagesByRank.computeIfAbsent(rank, k -> new ArrayList<>()).add(pkg);
                }
            }
            // Hidden links go between layer containers or, without them, one package per layer.
            Map<Integer, String> linkAliasByRank = new TreeMap<>();
            if (opt.isGroupLayers()) {
                Map<Integer, String> layerNames = new TreeMap<>();
                for (Map.Entry<String, Integer> layer : opt.getLayers().entrySet()) {
                    layerNames.putIfAbsent(layer.getValue(), layer.getKey());
                }
                for (Map.Entry<Integer, List<String>> entry : packagesByRank.entrySet()) {
                    String alias = "layer_" + entry.getKey();
                    linkAliasByRank.put(entry.getKey(), alias);
                    builder.append("package \"").append(layerNames.get(entry.getKey()))
                            .append("\" as ").append(alias).append(" {\n");
                    for (String pkg : entry.getValue()) {
                        appendPackage(builder, pkg, byPackage.get(pkg), "  ", opt);
                    }
                    builder.append("}\n\n");
                }
                for (String pkg : unranked) {
                    appendPackage(builder, pkg, byPackage.get(pkg), "", opt);
                }
            } else {
                for (Map.Entry<String, List<ClassModel>> entry : byPackage.entrySet()) {
                    Integer rank = opt.layerRankOf(entry.getKey());
                    if (rank != null) {
                        linkAliasByRank.putIfAbsent(rank, packageAlias(entry.getKey()));
                    }
                    appendPackage(builder, entry.getKey(), entry.getValue(), "", opt);
                }
            }
            appendLayerLinks(builder, linkAliasByRank);
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

        appendRelationships(builder, project, renderedNames(internals, externals), opt);
        builder.append("\n@enduml\n");
        return builder.toString();
    }

    /** PlantUML alias of a package block, usable in links. */
    private static String packageAlias(String packageName) {
        return "pkg_" + (packageName == null || packageName.isEmpty()
                ? "default" : packageName.replaceAll("[^\\p{L}\\p{N}]", "_"));
    }

    /** One {@code package "x.y" as pkg_x_y { }} block with its classes. */
    private void appendPackage(StringBuilder builder, String packageName, List<ClassModel> models,
                               String indent, DiagramOptions opt) {
        builder.append(indent).append("package \"").append(PackageModel.displayName(packageName))
                .append("\" as ").append(packageAlias(packageName)).append(" {\n");
        for (ClassModel model : models) {
            appendClass(builder, model, indent + "  ", opt);
        }
        builder.append(indent).append("}\n\n");
    }

    /** Hidden links between consecutive layers (their containers or one package each), to fix their order. */
    private static void appendLayerLinks(StringBuilder builder, Map<Integer, String> aliasByRank) {
        if (aliasByRank.size() < 2) {
            return;
        }
        builder.append("' ---------------- LAYER ORDER ----------------\n");
        String previous = null;
        for (String alias : aliasByRank.values()) {
            if (previous != null) {
                builder.append(previous).append(" -[hidden]down- ").append(alias).append("\n");
            }
            previous = alias;
        }
        builder.append("\n");
    }

    /** Ids of the rendered (non-hidden) classes. */
    private static Set<String> renderedNames(List<ClassModel> internals, List<ClassModel> externals) {
        Set<String> names = new HashSet<>();
        if (internals != null) {
            for (ClassModel model : internals) {
                names.add(model.id());
            }
        }
        if (externals != null) {
            for (ClassModel model : externals) {
                names.add(model.id());
            }
        }
        return names;
    }

    /** PlantUML name of a class: its name, or for a homonym its id with non-alphanumerics as {@code _}. */
    private static String aliasOf(ClassModel model) {
        if (Objects.equals(model.id(), model.getName())) {
            return model.getName();
        }
        return model.id().replaceAll("[^\\p{L}\\p{N}]", "_");
    }

    /** true for JDK packages ({@code java.*}), kept apart from third-party externals. */
    static boolean isJdkPackage(String packageName) {
        return packageName != null
                && (packageName.equals("java.lang") || packageName.startsWith("java."));
    }

    private void appendClass(StringBuilder builder, ClassModel model, String indent, DiagramOptions opt) {
        boolean isInterface = model.getKindEnum() == Kind.INTERFACE;
        builder.append(indent).append(keywordFor(model)).append(" ");
        String alias = aliasOf(model);
        if (Objects.equals(alias, model.getName())) {
            builder.append(alias);
        } else {
            builder.append('"').append(model.getName()).append("\" as ").append(alias);
        }
        builder.append(" {\n");

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
                        .append(visibilityOf(attribute.getModifiers(), isInterface))
                        .append(attribute.getName())
                        .append(": ")
                        .append(attribute.getType())
                        .append(modifierSuffix(attribute.getModifiers()))
                        .append("\n");
            }
        }

        if (opt.isShowConstructors() && model.getConstructors() != null) {
            for (ConstructorModel constructor : model.getConstructors()) {
                builder.append(indent).append("  ")
                        .append("\u00abcreate\u00bb ")
                        .append(visibilityOf(constructor.getModifiers(), false))
                        .append(constructor.getName())
                        .append("(")
                        .append(formatParameters(constructor.getParameters(), opt))
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
                        .append(visibilityOf(method.getModifiers(), isInterface))
                        .append(method.getName())
                        .append("(")
                        .append(formatParameters(method.getParameters(), opt))
                        .append("): ")
                        .append(method.getReturnType())
                        .append(modifierSuffix(method.getModifiers()))
                        .append("\n");
            }
        }

        builder.append(indent).append("}\n\n");
    }

    /**
     * Relationships that are drawn, in model order: dependencies allowed by the options and
     * endpoints not hidden. Relationships to unmodeled names are preserved: PlantUML
     * declares the missing endpoint implicitly.
     */
    static List<RelationshipModel> visibleRelationships(ProjectModel project, Set<String> renderedNames,
                                                        DiagramOptions opt) {
        List<RelationshipModel> visible = new ArrayList<>();
        if (project.getRelationships() == null) {
            return visible;
        }
        Map<String, String> simpleNames = new HashMap<>();
        if (project.getClasses() != null) {
            for (ClassModel model : project.getClasses()) {
                simpleNames.put(model.id(), model.getName());
            }
        }
        // Endpoints hidden by DiagramOptions (known but not rendered).
        Set<String> hiddenNames = new HashSet<>(simpleNames.keySet());
        hiddenNames.removeAll(renderedNames);
        for (RelationshipModel relationship : project.getRelationships()) {
            if (RelType.DEPENDENCY.label().equals(relationship.getType())
                    && !opt.showsDependency(simpleNames.get(relationship.getSource()),
                            simpleNames.get(relationship.getTarget()))) {
                continue;
            }
            if (hiddenNames.contains(relationship.getSource())
                    || hiddenNames.contains(relationship.getTarget())) {
                continue;
            }
            visible.add(relationship);
        }
        return visible;
    }

    private void appendRelationships(StringBuilder builder, ProjectModel project, Set<String> renderedNames,
                                     DiagramOptions opt) {
        Set<String> externalNames = new HashSet<>();
        Map<String, String> aliases = new HashMap<>();
        Map<String, Integer> ranks = new HashMap<>();
        if (project.getClasses() != null) {
            for (ClassModel model : project.getClasses()) {
                Integer rank = model.isExternal() ? null : opt.layerRankOf(model.getPackageName());
                if (rank != null) {
                    ranks.put(model.id(), rank);
                }
                aliases.put(model.id(), aliasOf(model));
                if (model.isExternal() && renderedNames.contains(model.id())) {
                    externalNames.add(model.id());
                }
            }
        }
        List<RelationshipModel> internalRels = new ArrayList<>();
        List<RelationshipModel> externalRels = new ArrayList<>();
        for (RelationshipModel relationship : visibleRelationships(project, renderedNames, opt)) {
            if (externalNames.contains(relationship.getSource())
                    || externalNames.contains(relationship.getTarget())) {
                externalRels.add(relationship);
            } else {
                internalRels.add(relationship);
            }
        }
        for (RelationshipModel relationship : internalRels) {
            appendRelationship(builder, relationship, aliases, ranks);
        }
        if (!externalRels.isEmpty()) {
            builder.append("' ---------------- EXTERNAL RELATIONSHIPS ----------------\n");
            for (RelationshipModel relationship : externalRels) {
                appendRelationship(builder, relationship, aliases, ranks);
            }
        }
    }

    private void appendRelationship(StringBuilder builder, RelationshipModel relationship,
                                    Map<String, String> aliases, Map<String, Integer> ranks) {
        // In PlantUML the triangle head points at the parent:
        // "Parent <|-- Child", "Interface <|.. Implementation".
        // Our model stores source=child, target=parent, so swap them here.
        String type = relationship.getType();
        boolean inheritance = RelType.EXTENDS.label().equals(type) || RelType.IMPLEMENTS.label().equals(type);
        String left = inheritance ? relationship.getTarget() : relationship.getSource();
        String right = inheritance ? relationship.getSource() : relationship.getTarget();
        // Unmodeled endpoints have no alias and are written as they come.
        builder.append(aliases.getOrDefault(left, left))
                .append(" ")
                .append(withDirection(arrowFor(type), ranks.get(left), ranks.get(right)))
                .append(" ")
                .append(aliases.getOrDefault(right, right))
                .append("\n");
    }

    /**
     * Adds {@code down}/{@code up} to an arrow so the right element sits below/above the left one
     * by layer rank: {@code -->} becomes {@code -down->}, {@code <|..} becomes {@code <|.down.}.
     * Same or unknown layer: arrow unchanged.
     */
    static String withDirection(String arrow, Integer leftRank, Integer rightRank) {
        if (leftRank == null || rightRank == null || leftRank.equals(rightRank)) {
            return arrow;
        }
        String direction = rightRank > leftRank ? "down" : "up";
        if (arrow.contains("--")) {
            return arrow.replaceFirst("--", "-" + direction + "-");
        }
        return arrow.replaceFirst("\\.\\.", "." + direction + ".");
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

    private String formatParameters(List<ParameterModel> parameters, DiagramOptions opt) {
        if (parameters == null || parameters.isEmpty()) {
            return "";
        }
        if (opt.isShortSignatures() && parameters.size() > DiagramOptions.SHORT_SIGNATURE_MAX_PARAMS) {
            // Keep the count so overloads stay distinguishable: Estudiante(\u20267).
            return "\u2026" + parameters.size();
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < parameters.size(); i++) {
            ParameterModel parameter = parameters.get(i);
            builder.append(parameter.getName()).append(": ").append(parameter.getType());
            if (i < parameters.size() - 1) {
                builder.append(", ");
            }
        }
        return builder.toString();
    }

    /** Interface members without modifier are implicitly public in Java, so they render as {@code +}. */
    private String visibilityOf(List<String> modifiers, boolean implicitPublic) {
        String fallback = implicitPublic ? "+" : "~";
        if (modifiers == null) {
            return fallback;
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
        return fallback;
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