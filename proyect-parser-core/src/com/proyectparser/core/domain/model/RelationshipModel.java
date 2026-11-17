package com.proyectparser.core.domain.model;

/**
 * Directed relationship with a kind from {@link RelType}.
 * Source and target are class ids ({@link ClassModel#id()}).
 */
public class RelationshipModel {
    private String source;
    private String target;
    private String type;
    private String targetType;

    /** Declared supertype expression, including generic arguments; identity remains target. */
    public String getTargetType() { return targetType; }
    public RelationshipModel(String source, String target, String type, String targetType) {
        this(source, target, type);
        this.targetType = targetType;
    }

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
