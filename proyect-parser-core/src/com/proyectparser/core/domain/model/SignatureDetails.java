package com.proyectparser.core.domain.model;

import java.util.List;

/** Source-level information shared by methods and constructors. No AST dependency. */
public record SignatureDetails(List<String> typeParameterDeclarations,
                               List<String> typeParameterBounds, List<String> thrownTypes) {
    public SignatureDetails {
        typeParameterDeclarations = List.copyOf(typeParameterDeclarations);
        typeParameterBounds = List.copyOf(typeParameterBounds);
        thrownTypes = List.copyOf(thrownTypes);
    }
    public static SignatureDetails empty() { return new SignatureDetails(List.of(), List.of(), List.of()); }
    public String genericPrefix() {
        return typeParameterDeclarations.isEmpty() ? "" : "<" + String.join(", ", typeParameterDeclarations) + "> ";
    }
    public String throwsSuffix() {
        return thrownTypes.isEmpty() ? "" : " throws " + String.join(", ", thrownTypes);
    }
}
