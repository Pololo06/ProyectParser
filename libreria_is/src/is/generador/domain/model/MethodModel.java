package is.generador.domain.model;

import java.util.List;

/** POJO without JavaParser/PlantUML dependencies (Fase 1). */
public class MethodModel {
    private String name;
    private String returnType;
    private List<ParameterModel> parameters;
    private List<String> modifiers;

    public MethodModel(String name, String returnType, List<ParameterModel> parameters, List<String> modifiers) {
        this.name = name;
        this.returnType = returnType;
        this.parameters = parameters;
        this.modifiers = modifiers;
    }

    public String getName() { return name; }
    public String getReturnType() { return returnType; }
    public List<ParameterModel> getParameters() { return parameters; }
    public List<String> getModifiers() { return modifiers; }
}
