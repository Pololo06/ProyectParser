package com.proyectparser.server.dto;

import java.util.List;

/** HTTP representation of a Java package view. */
public record PackageDto(
        String name,
        String displayName,
        int typeCount,
        List<String> types
) {
}
