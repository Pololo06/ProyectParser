package is.generador.domain.policy;

/**
 * Interruptores de visualización para el diagrama (Fase 2).
 * Inmutable: se construye con {@link Builder}. Pertenece a
 * {@code domain.policy}: no depende de infraestructura.
 *
 * <p>Todos los flags son {@code true} por defecto (diagrama completo).
 * Los getters/setters se detectan con la regla estricta de
 * {@link is.generador.domain.BeanAccessors} (método con campo respaldo real).</p>
 *
 * <pre>
 * DiagramOptions opciones = new DiagramOptions.Builder()
 *     .showGettersSetters(false) // oculta getters/setters para no ensuciar
 *     .showExternal(true)
 *     .build();
 * </pre>
 */
public class DiagramOptions {

    private final boolean showGettersSetters;
    private final boolean showAttributes;
    private final boolean showMethods;
    private final boolean showConstructors;
    private final boolean showExternal;
    private final boolean showJdkTypes;
    private final boolean groupByPackage;

    private DiagramOptions(Builder builder) {
        this.showGettersSetters = builder.showGettersSetters;
        this.showAttributes = builder.showAttributes;
        this.showMethods = builder.showMethods;
        this.showConstructors = builder.showConstructors;
        this.showExternal = builder.showExternal;
        this.showJdkTypes = builder.showJdkTypes;
        this.groupByPackage = builder.groupByPackage;
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
}
