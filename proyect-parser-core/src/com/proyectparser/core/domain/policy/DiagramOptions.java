package com.proyectparser.core.domain.policy;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Interruptores de visualización para el diagrama (Fase 2).
 * Inmutable: se construye con {@link Builder}. Pertenece a
 * {@code domain.policy}: no depende de infraestructura.
 *
 * <p>Todos los flags son {@code true} por defecto (diagrama completo).
 * Los getters/setters se detectan con la regla estricta de
 * {@link com.proyectparser.core.domain.BeanAccessors} (método con campo respaldo real).</p>
 *
 * <pre>
 * DiagramOptions opciones = new DiagramOptions.Builder()
 *     .showGettersSetters(false) // oculta getters/setters para no ensuciar
 *     .showExternal(true)
 *     .build();
 * </pre>
 */
public class DiagramOptions {

    /**
     * Clean Architecture layer order used by {@code --capas} without a value (top to bottom).
     * {@code infrastructure.config} (composition root) goes first: it creates everything below.
     */
    /** With short signatures, members with more parameters than this show {@code (…n)}. */
    public static final int SHORT_SIGNATURE_MAX_PARAMS = 3;

    public static final List<String> DEFAULT_LAYERS = List.of(
            "infrastructure.config", "infrastructure.cli", "interfaceadapters", "application", "domain",
            "infrastructure.persistence");

    /** Relationship line style ({@code skinparam linetype}); {@code DEFAULT} emits nothing. */
    public enum LineType {
        DEFAULT, ORTHO, POLYLINE, SPLINE;

        /** Parses "ortho|polyline|spline"; null/unknown gives {@code null}. */
        public static LineType fromLabel(String label) {
            if (label == null) {
                return null;
            }
            for (LineType type : values()) {
                if (type != DEFAULT && type.name().equalsIgnoreCase(label.trim())) {
                    return type;
                }
            }
            return null;
        }
    }

    private final boolean showGettersSetters;
    private final boolean showAttributes;
    private final boolean showMethods;
    private final boolean showConstructors;
    private final boolean showExternal;
    private final boolean showJdkTypes;
    private final boolean groupByPackage;
    private final boolean showDependencies;
    private final boolean shortSignatures;
    private final boolean hideOrphans;
    private final boolean summary;
    private final Set<String> allowedDependencies;
    private final LineType lineType;
    private final Map<String, Integer> layers;

    private DiagramOptions(Builder builder) {
        this.showGettersSetters = builder.showGettersSetters;
        this.showAttributes = builder.showAttributes;
        this.showMethods = builder.showMethods;
        this.showConstructors = builder.showConstructors;
        this.showExternal = builder.showExternal;
        this.showJdkTypes = builder.showJdkTypes;
        this.groupByPackage = builder.groupByPackage;
        this.showDependencies = builder.showDependencies;
        this.shortSignatures = builder.shortSignatures;
        this.hideOrphans = builder.hideOrphans;
        this.summary = builder.summary;
        this.allowedDependencies = Collections.unmodifiableSet(new TreeSet<>(builder.allowedDependencies));
        this.lineType = builder.lineType;
        this.layers = Collections.unmodifiableMap(new LinkedHashMap<>(builder.layers));
    }

    /** Opciones por defecto: todo visible y agrupado por paquete. */
    public static DiagramOptions defaults() {
        return new Builder().build();
    }

    public static class Builder {
        private boolean showGettersSetters = true;
        private boolean showAttributes = true;
        private boolean showMethods = true;
        private boolean showConstructors = true;
        private boolean showExternal = true;
        private boolean showJdkTypes = true;
        private boolean groupByPackage = true;
        private boolean showDependencies = true;
        private boolean shortSignatures = false;
        private boolean hideOrphans = false;
        private boolean summary = false;
        private Set<String> allowedDependencies = new TreeSet<>();
        private LineType lineType = LineType.DEFAULT;
        private Map<String, Integer> layers = new LinkedHashMap<>();

        /** Copia los valores de unas opciones existentes. */
        public Builder(DiagramOptions base) {
            if (base != null) {
                this.showGettersSetters = base.showGettersSetters;
                this.showAttributes = base.showAttributes;
                this.showMethods = base.showMethods;
                this.showConstructors = base.showConstructors;
                this.showExternal = base.showExternal;
                this.showJdkTypes = base.showJdkTypes;
                this.groupByPackage = base.groupByPackage;
                this.showDependencies = base.showDependencies;
                this.shortSignatures = base.shortSignatures;
                this.hideOrphans = base.hideOrphans;
                this.summary = base.summary;
                this.allowedDependencies = new TreeSet<>(base.allowedDependencies);
                this.lineType = base.lineType;
                this.layers = new LinkedHashMap<>(base.layers);
            }
        }

        public Builder() {
        }

        public Builder showGettersSetters(boolean show) { this.showGettersSetters = show; return this; }
        public Builder showAttributes(boolean show) { this.showAttributes = show; return this; }
        public Builder showMethods(boolean show) { this.showMethods = show; return this; }
        public Builder showConstructors(boolean show) { this.showConstructors = show; return this; }
        public Builder showExternal(boolean show) { this.showExternal = show; return this; }
        public Builder showJdkTypes(boolean show) { this.showJdkTypes = show; return this; }
        public Builder groupByPackage(boolean group) { this.groupByPackage = group; return this; }
        /** false omits the {@code ..>} dependency arrows (usually implied by fields/interfaces). */
        /**
         * Layer ranks: package segment ({@code "domain"}, {@code "infrastructure.cli"}) to rank,
         * 0 at the top. Empty disables layer-directed arrows and hidden links.
         */
        public Builder layers(Map<String, Integer> ranks) {
            this.layers = ranks == null ? new LinkedHashMap<>() : new LinkedHashMap<>(ranks);
            return this;
        }
        /** Layers ranked by their position in the list. */
        public Builder layerOrder(List<String> segments) {
            Map<String, Integer> ranks = new LinkedHashMap<>();
            if (segments != null) {
                for (String segment : segments) {
                    ranks.putIfAbsent(segment.trim(), ranks.size());
                }
            }
            return layers(ranks);
        }
        public Builder lineType(LineType type) { this.lineType = type == null ? LineType.DEFAULT : type; return this; }
        /**
         * {@code ..>} that survive {@code showDependencies(false)}, as {@link #dependencyKey} pairs
         * (e.g. those chosen for a {@code --modulo}). Empty: none survive.
         */
        public Builder allowedDependencies(Set<String> keys) {
            this.allowedDependencies = keys == null ? new TreeSet<>() : new TreeSet<>(keys);
            return this;
        }
        /** true shows {@code (…n)}, n = parameter count, for methods/constructors with more than {@link #SHORT_SIGNATURE_MAX_PARAMS} parameters. */
        public Builder shortSignatures(boolean shorten) { this.shortSignatures = shorten; return this; }
        /** true omits classes with no visible relationship once options and filters apply. */
        public Builder hideOrphans(boolean hide) { this.hideOrphans = hide; return this; }
        /** true emits {@code hide members}: an overview with class names only. */
        public Builder summary(boolean summary) { this.summary = summary; return this; }
        public Builder showDependencies(boolean show) { this.showDependencies = show; return this; }

        public DiagramOptions build() {
            return new DiagramOptions(this);
        }
    }

    public boolean isShowGettersSetters() { return showGettersSetters; }
    public boolean isShowAttributes() { return showAttributes; }
    public boolean isShowMethods() { return showMethods; }
    public boolean isShowConstructors() { return showConstructors; }
    public boolean isShowExternal() { return showExternal; }
    public boolean isShowJdkTypes() { return showJdkTypes; }
    public boolean isGroupByPackage() { return groupByPackage; }
    public Map<String, Integer> getLayers() { return layers; }

    /**
     * Rank of a package: the longest layer segment it contains as whole segments
     * ({@code "domain"} matches {@code com.x.domain.model}); {@code null} if none.
     */
    public Integer layerRankOf(String packageName) {
        if (packageName == null || layers.isEmpty()) {
            return null;
        }
        String dotted = "." + packageName + ".";
        String best = null;
        for (String segment : layers.keySet()) {
            if (dotted.contains("." + segment + ".") && (best == null || segment.length() > best.length())) {
                best = segment;
            }
        }
        return best == null ? null : layers.get(best);
    }

    public LineType getLineType() { return lineType; }
    public Set<String> getAllowedDependencies() { return allowedDependencies; }

    /** Key of a dependency between two class names: {@code "Source->Target"}. */
    public static String dependencyKey(String sourceName, String targetName) {
        return sourceName + "->" + targetName;
    }

    /** true if a dependency between these class names is rendered. */
    public boolean showsDependency(String sourceName, String targetName) {
        return showDependencies || allowedDependencies.contains(dependencyKey(sourceName, targetName));
    }

    public boolean isSummary() { return summary; }
    public boolean isHideOrphans() { return hideOrphans; }
    public boolean isShortSignatures() { return shortSignatures; }
    public boolean isShowDependencies() { return showDependencies; }
}
