package com.proyectparser.server.dto;

import java.util.List;

/** HTTP representation of a Java constructor. */
public record ConstructorDto(
        String name,
        List<ParameterDto> parameters,
        List<String> modifiers,
        List<String> typeParameters
) {
}
