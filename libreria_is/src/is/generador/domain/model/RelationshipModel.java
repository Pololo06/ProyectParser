package is.generador.domain.model;

/** Relationship kind: EXTENDS, IMPLEMENTS, ASSOCIATION, DEPENDENCY. */
public class RelationshipModel {
    private String source;
    private String target;
    private String type;

    public RelationshipModel(String source, String target, String type) {
        this.source = source;
        this.target = target;
        this.type = type;
    }

    public String getSource() { return source; }
    public String getTarget() { return target; }
    public String getType() { return type; }
    /** Clave canónica {@code "origen|TIPO|destino"}. */
    public String key() { return source + "|" + type + "|" + target; }
    public RelType getTypeEnum() { return RelType.fromLabel(type); }
}
