package com.proyectparser.server.dto;

/** HTTP representation of a Java attribute/field. */
public record AttributeDto(
        String name,
        String type,
        java.util.List<String> modifiers,
        String initialValue
) {
}
