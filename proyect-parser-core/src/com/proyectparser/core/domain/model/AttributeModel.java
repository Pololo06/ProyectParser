package com.proyectparser.core.domain.model;

import java.util.List;

/** POJO without JavaParser/PlantUML dependencies (Fase 1). */
public class AttributeModel {
    private String name;
    private String type;
    private List<String> modifiers;
    private String initialValue;
    private boolean initializedWithNew;

    public AttributeModel(String name, String type, List<String> modifiers) {
        this(name, type, modifiers, null);
    }

    public AttributeModel(String name, String type, List<String> modifiers, String initialValue) {
        this(name, type, modifiers, initialValue, false);
    }

    public AttributeModel(String name, String type, List<String> modifiers,
                          String initialValue, boolean initializedWithNew) {
        this.initializedWithNew = initializedWithNew;
        this.name = name;
        this.type = type;
        this.modifiers = modifiers;
        this.initialValue = initialValue;
    }

    public String getName() { return name; }
    public String getType() { return type; }
    public List<String> getModifiers() { return modifiers; }
    /** Initializer as written in the declaration ({@code 5000}), or {@code null} if it has none. */
    public boolean isInitializedWithNew() { return initializedWithNew; }
    public String getInitialValue() { return initialValue; }
}
