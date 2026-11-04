package com.proyectparser.server.dto;

import java.util.List;

/**
 * Display switches, mirroring the core {@code DiagramOptions}. Every field is
 * optional: {@code null} means "use the library default" (all flags are
 * {@code true} except the experimental ones).
 */
public record OptionsRequest(
        Boolean showGettersSetters,
        Boolean showAttributes,
        Boolean showMethods,
        Boolean showConstructors,
        Boolean showExternal,
        Boolean showJdkTypes,
        Boolean showDependencies,
        Boolean groupByPackage,
        Boolean shortSignatures,
        Boolean hideOrphans,
        Boolean summary,
        Boolean groupLayers,
        String lineType,
        List<String> layerOrder,
        List<String> allowedDependencies
) {
}
