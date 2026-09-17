package is.generador.core;

import java.util.Map;
import java.util.Set;

public class TypeClassifier {

    private static final Set<String> PRIMITIVES = Set.of(
        "byte", "short", "int", "long", "float", "double", "boolean", "char", "void"
    );

    // Tipos básicos que actúan como escalares/literales (no deben generar caja externa)
    private static final Set<String> BASIC_SCALARS = Set.of(
        "String", "Object", "Class",
        "Byte", "Short", "Integer", "Long",
        "Float", "Double", "Boolean", "Character", "Void"
    );

    private static final Map<String, String> COMMON_JDK_TYPES = Map.ofEntries(
        Map.entry("UUID", "java.util"),
        Map.entry("Date", "java.util"),
        Map.entry("Optional", "java.util"),
        Map.entry("List", "java.util"),
        Map.entry("Set", "java.util"),
        Map.entry("Map", "java.util"),
        Map.entry("BigDecimal", "java.math"),
        Map.entry("BigInteger", "java.math"),
        Map.entry("LocalDate", "java.time"),
        Map.entry("LocalDateTime", "java.time"),
        Map.entry("LocalTime", "java.time"),
        Map.entry("OffsetDateTime", "java.time"),
        Map.entry("ZonedDateTime", "java.time"),
        Map.entry("Instant", "java.time"),
        Map.entry("Duration", "java.time"),
        Map.entry("Period", "java.time")
    );

    public static boolean shouldIgnoreBox(String rawType) {
        String clean = extractTargetType(rawType);
        return clean.isBlank() || PRIMITIVES.contains(clean) || BASIC_SCALARS.contains(clean);
    }

    public static String extractTargetType(String rawType) {
        if (rawType == null) return "";
        String clean = rawType.trim();
        // Bucle recursivo para genéricos anidados (ej. Map<String, List<UUID>> -> UUID)
        while (clean.contains("<") && clean.contains(">")) {
            int start = clean.indexOf('<');
            int end = clean.lastIndexOf('>');
            clean = clean.substring(start + 1, end).trim();
            if (clean.contains(",")) {
                String[] parts = clean.split(",");
                clean = parts[parts.length - 1].trim();
            }
        }
        return clean.replace("[]", "").trim();
    }

    public static String resolvePackage(String typeName) {
        if (COMMON_JDK_TYPES.containsKey(typeName)) {
            return COMMON_JDK_TYPES.get(typeName);
        }
        if (typeName.contains(".")) {
            return typeName.substring(0, typeName.lastIndexOf('.'));
        }
        return "java.lang";
    }
}
