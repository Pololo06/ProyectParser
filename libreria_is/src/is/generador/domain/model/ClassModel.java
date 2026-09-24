package is.generador.domain.model;

import java.util.List;

/**
 * Canonical domain type. {@code kind} is one of
 * Class, AbstractClass, Interface, Enum, Record, Annotation.
 * Typed views {@link InterfaceModel}, {@link EnumModel} and {@link RecordModel}
 * exist to satisfy the spec literally; they fix {@code kind} accordingly.
 */
public class ClassModel {
    private String name;
    private String packageName;
    private String kind;
    private boolean isAbstract;
    private List<String> stereotypes;
    private List<AttributeModel> attributes;
    private List<MethodModel> methods;
    private List<ConstructorModel> constructors;
    private List<String> extendedTypes;
    private List<String> implementedTypes;
    private List<String> enumConstants;

    public ClassModel(String name, String packageName, String kind, boolean isAbstract,
                      List<String> stereotypes, List<AttributeModel> attributes,
                      List<MethodModel> methods, List<ConstructorModel> constructors,
                      List<String> extendedTypes, List<String> implementedTypes,
                      List<String> enumConstants) {
        this.name = name;
        this.packageName = packageName;
        this.kind = kind;
        this.isAbstract = isAbstract;
        this.stereotypes = stereotypes;
        this.attributes = attributes;
        this.methods = methods;
        this.constructors = constructors;
        this.extendedTypes = extendedTypes;
        this.implementedTypes = implementedTypes;
        this.enumConstants = enumConstants;
    }

    public String getName() { return name; }
    public String getPackageName() { return packageName; }
    public String getKind() { return kind; }
    public Kind getKindEnum() { return Kind.fromLabel(kind); }
    /** Fully-qualified name ({@code package.Name} or just {@code Name}). */
    public String getFqn() {
        if (packageName == null || packageName.isBlank()) {
            return name;
        }
        return packageName + "." + name;
    }
    public boolean isAbstract() { return isAbstract; }
    public List<String> getStereotypes() { return stereotypes; }
    public List<AttributeModel> getAttributes() { return attributes; }
    public List<MethodModel> getMethods() { return methods; }
    public List<ConstructorModel> getConstructors() { return constructors; }
    public List<String> getExtendedTypes() { return extendedTypes; }
    public List<String> getImplementedTypes() { return implementedTypes; }
    public List<String> getEnumConstants() { return enumConstants; }
}
