package is.generador.application;

import java.util.List;

/**
 * Vista de detalle de un tipo analizado (paso 8 del split).
 * Sale de {@code SoyLaPuerta}: dava clase interna, ahora pública en application.
 */
public class ClassInfo {
    private final String name;
    private final String classType;
    private final List<String> stereotypes;
    private final List<String> properties;
    private final List<String> constructors;
    private final List<String> getters;
    private final List<String> setters;

    public ClassInfo(String name, String classType, List<String> stereotypes,
                     List<String> properties, List<String> constructors,
                     List<String> getters, List<String> setters) {
        this.name = name;
        this.classType = classType;
        this.stereotypes = stereotypes;
        this.properties = properties;
        this.constructors = constructors;
        this.getters = getters;
        this.setters = setters;
    }

    public String getName() { return name; }
    public String getClassType() { return classType; }
    public List<String> getStereotypes() { return stereotypes; }
    public List<String> getProperties() { return properties; }
    public List<String> getConstructors() { return constructors; }
    public List<String> getGetters() { return getters; }
    public List<String> getSetters() { return setters; }

    // Spanish aliases (backward compatibility)
    public String getNombre() { return name; }
    public String getTipoClase() { return classType; }
    public List<String> getEstereotipos() { return stereotypes; }
    public List<String> getPropiedades() { return properties; }
    public List<String> getConstructores() { return constructors; }
}
