package is.generador.core.model;

/** POJO without JavaParser/PlantUML dependencies (Fase 1). */
public class ParameterModel {
    private String name;
    private String type;

    public ParameterModel(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public String getName() { return name; }
    public String getType() { return type; }
}
