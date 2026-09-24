package is.generador.domain.model;

import java.util.List;

/** POJO without JavaParser/PlantUML dependencies (Fase 1). */
public class ConstructorModel {
    private String name;
    private List<ParameterModel> parameters;
    private List<String> modifiers;
    private List<String> typeParameters;

    public ConstructorModel(String name, List<ParameterModel> parameters,
                            List<String> modifiers, List<String> typeParameters) {
        this.name = name;
        this.parameters = parameters;
        this.modifiers = modifiers;
        this.typeParameters = typeParameters;
    }

    public String getName() { return name; }
    public List<ParameterModel> getParameters() { return parameters; }
    public List<String> getModifiers() { return modifiers; }
    /** Constructor-level type variables; never rendered as externals. */
    public List<String> getTypeParameters() { return typeParameters; }
}
