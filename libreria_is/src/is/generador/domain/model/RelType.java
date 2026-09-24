package is.generador.domain.model;

/** Canonical relationship kinds. Replaces magic type strings. */
public enum RelType {
    EXTENDS("EXTENDS"),
    IMPLEMENTS("IMPLEMENTS"),
    ASSOCIATION("ASSOCIATION"),
    DEPENDENCY("DEPENDENCY");

    private final String label;

    RelType(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public static RelType fromLabel(String label) {
        if (label == null) {
            return ASSOCIATION;
        }
        for (RelType rel : values()) {
            if (rel.label.equalsIgnoreCase(label)) {
                return rel;
            }
        }
        return ASSOCIATION;
    }
}
