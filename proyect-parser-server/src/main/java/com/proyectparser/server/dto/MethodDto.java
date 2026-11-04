package com.proyectparser.server.dto;

import java.util.List;

/** HTTP representation of a Java method. */
public record MethodDto(
        String name,
        String returnType,
        List<ParameterDto> parameters,
        List<String> modifiers,
        List<String> typeParameters
) {
}
