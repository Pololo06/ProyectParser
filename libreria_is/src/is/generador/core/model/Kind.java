package is.generador.core.model;

/** Canonical classifier kinds. Replaces magic {@code kind} strings. */
public enum Kind {
    CLASS("Class"),
    ABSTRACT_CLASS("AbstractClass"),
    INTERFACE("Interface"),
    ENUM("Enum"),
    RECORD("Record"),
    ANNOTATION("Annotation");

    private final String label;

    Kind(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public static Kind fromLabel(String label) {
        if (label == null) {
            return CLASS;
        }
        for (Kind kind : values()) {
            if (kind.label.equalsIgnoreCase(label)) {
                return kind;
            }
        }
        return CLASS;
    }
}
