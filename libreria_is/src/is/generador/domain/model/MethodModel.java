package is.generador.domain.model;

import java.util.List;

/** POJO without JavaParser/PlantUML dependencies (Fase 1). */
public class MethodModel {
    private String name;
    private String returnType;
    private List<ParameterModel> parameters;
    private List<String> modifiers;
    private List<String> typeParameters;

    public MethodModel(String name, String returnType, List<ParameterModel> parameters,
                       List<String> modifiers, List<String> typeParameters) {
        this.name = name;
        this.returnType = returnType;
        this.parameters = parameters;
        this.modifiers = modifiers;
        this.typeParameters = typeParameters;
    }

    public String getName() { return name; }
    public String getReturnType() { return returnType; }
    public List<ParameterModel> getParameters() { return parameters; }
    public List<String> getModifiers() { return modifiers; }
    /** Method-level type variables (e.g. {@code <T>}); never rendered as externals. */
    public List<String> getTypeParameters() { return typeParameters; }
}
