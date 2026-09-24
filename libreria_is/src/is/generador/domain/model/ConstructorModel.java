package is.generador.domain.model;

import java.util.List;

/** POJO without JavaParser/PlantUML dependencies (Fase 1). */
public class ConstructorModel {
    private String name;
    private List<ParameterModel> parameters;
    private List<String> modifiers;

    public ConstructorModel(String name, List<ParameterModel> parameters, List<String> modifiers) {
        this.name = name;
        this.parameters = parameters;
        this.modifiers = modifiers;
    }

    public String getName() { return name; }
    public List<ParameterModel> getParameters() { return parameters; }
    public List<String> getModifiers() { return modifiers; }
}
