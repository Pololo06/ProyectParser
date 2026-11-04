package com.proyectparser.server.dto;

import java.util.List;

/**
 * HTTP representation of a parsed Java type.
 *
 * <p>{@code id} is the identifier used by relationships: the simple name when
 * it is unique in the project, or the fully-qualified name for homonyms.</p>
 */
public record ClassDto(
        String id,
        String name,
        String fqn,
        String packageName,
        String kind,
        boolean abstractType,
        boolean external,
        List<String> stereotypes,
        List<AttributeDto> attributes,
        List<MethodDto> methods,
        List<ConstructorDto> constructors,
        List<String> extendedTypes,
        List<String> implementedTypes,
        List<String> enumConstants,
        List<String> typeParameters
) {
}
