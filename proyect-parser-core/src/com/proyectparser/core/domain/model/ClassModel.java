package com.proyectparser.core.domain.model;

import java.util.List;

/**
 * Canonical domain type. {@code kind} is one of
 * Class, AbstractClass, Interface, Enum, Record, Annotation.
 *
 * <p>Identity: {@link #getFqn()} includes the enclosing types of a nested type
 * ({@code p.Outer.Inner}); {@link #id()} is the simple name while no other class
 * of the project shares it, and the fqn otherwise.
 */
public class ClassModel {
    /** Estereotipo que marca una clase como externa. */
    public static final String EXTERNAL_STEREOTYPE = "@external";
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
    private List<String> typeParameters;
    private String fqn;
    private String id;

    public ClassModel(String name, String packageName, String kind, boolean isAbstract,
                      List<String> stereotypes, List<AttributeModel> attributes,
                      List<MethodModel> methods, List<ConstructorModel> constructors,
                      List<String> extendedTypes, List<String> implementedTypes,
                      List<String> enumConstants, List<String> typeParameters) {
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
        this.typeParameters = typeParameters;
        this.fqn = packageName == null || packageName.isBlank() ? name : packageName + "." + name;
        this.id = name;
    }

    /** Copy with another fqn, e.g. {@code p.Outer.Inner} for a nested type. */
    public ClassModel withFqn(String fqn) {
        ClassModel copy = copy();
        copy.fqn = fqn;
        return copy;
    }

    /** Copy with another id (see {@link #id()}). */
    public ClassModel withId(String id) {
        ClassModel copy = copy();
        copy.id = id;
        return copy;
    }

    private ClassModel copy() {
        ClassModel copy = new ClassModel(name, packageName, kind, isAbstract, stereotypes, attributes,
                methods, constructors, extendedTypes, implementedTypes, enumConstants, typeParameters);
        copy.fqn = fqn;
        copy.id = id;
        return copy;
    }

    public String getName() { return name; }
    public String getPackageName() { return packageName; }
    public String getKind() { return kind; }
    public Kind getKindEnum() { return Kind.fromLabel(kind); }
    /**
     * Fully-qualified name: {@code package.Name} (or just {@code Name}), with the
     * enclosing types of a nested type ({@code package.Outer.Name}) when the analyzer sets them.
     */
    public String getFqn() { return fqn; }
    /**
     * Identifier of the class in its project: the simple name while it is unique among
     * all the classes (internal and external), the fqn when another class shares it.
     * Relationship endpoints and the keys of the queries use it. Defaults to the name.
     */
    public String id() { return id; }
    public boolean isAbstract() { return isAbstract; }
    public List<String> getStereotypes() { return stereotypes; }
    /** true si la clase es externa (estereotipo {@code @external}). */
    public boolean isExternal() {
        return stereotypes != null && stereotypes.contains(EXTERNAL_STEREOTYPE);
    }
    public List<AttributeModel> getAttributes() { return attributes; }
    public List<MethodModel> getMethods() { return methods; }
    public List<ConstructorModel> getConstructors() { return constructors; }
    public List<String> getExtendedTypes() { return extendedTypes; }
    public List<String> getImplementedTypes() { return implementedTypes; }
    public List<String> getEnumConstants() { return enumConstants; }
    /** Type variables declared by this type (e.g. {@code <T, ID>}); never rendered as externals. */
    public List<String> getTypeParameters() { return typeParameters; }
}
