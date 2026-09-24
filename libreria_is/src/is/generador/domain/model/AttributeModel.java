package is.generador.domain.model;

import java.util.List;

/** POJO without JavaParser/PlantUML dependencies (Fase 1). */
public class AttributeModel {
    private String name;
    private String type;
    private List<String> modifiers;

    public AttributeModel(String name, String type, List<String> modifiers) {
        this.name = name;
        this.type = type;
        this.modifiers = modifiers;
    }

    public String getName() { return name; }
    public String getType() { return type; }
    public List<String> getModifiers() { return modifiers; }
}
