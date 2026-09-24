package is.generador.core;

import java.util.LinkedHashSet;
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
        Map.entry("ArrayList", "java.util"),
        Map.entry("LinkedList", "java.util"),
        Map.entry("Set", "java.util"),
        Map.entry("HashSet", "java.util"),
        Map.entry("Map", "java.util"),
        Map.entry("HashMap", "java.util"),
        Map.entry("Collection", "java.util"),
        Map.entry("BigDecimal", "java.math"),
        Map.entry("BigInteger", "java.math"),
        Map.entry("LocalDate", "java.time"),
        Map.entry("LocalDateTime", "java.time"),
        Map.entry("LocalTime", "java.time"),
        Map.entry("OffsetDateTime", "java.time"),
        Map.entry("ZonedDateTime", "java.time"),
        Map.entry("Instant", "java.time"),
        Map.entry("Duration", "java.time"),
        Map.entry("Period", "java.time"),
        Map.entry("Path", "java.nio.file"),
        Map.entry("Paths", "java.nio.file"),
        Map.entry("Files", "java.nio.file"),
        Map.entry("IOException", "java.io"),
        Map.entry("File", "java.io")
    );

    /** Container types whose own box is noise; only their type arguments matter. */
    private static final Set<String> CONTAINERS = Set.of(
        "List", "Set", "Map", "Collection", "Optional",
        "ArrayList", "LinkedList", "HashSet", "HashMap"
    );

    public static boolean shouldIgnoreBox(String rawType) {
        if (rawType == null || rawType.isBlank()) {
            return true;
        }
        // Ignorable only if EVERY referenced leaf type is primitive/scalar.
        Set<String> refs = referencedTypeNames(rawType);
        if (refs.isEmpty()) {
            return true;
        }
        for (String ref : refs) {
            if (CONTAINERS.contains(ref)) {
                continue;
            }
            if (!PRIMITIVES.contains(ref) && !BASIC_SCALARS.contains(ref)) {
                return false;
            }
        }
        return true;
    }

    public static String extractTargetType(String rawType) {
        Set<String> refs = referencedTypeNames(rawType);
        if (refs.isEmpty()) {
            return "";
        }
        // Backward-compatible: last non-container leaf (e.g. Map<String,List<UUID>> -> UUID).
        String last = "";
        for (String ref : refs) {
            if (!CONTAINERS.contains(ref)) {
                last = ref;
            }
        }
        return last.isEmpty() ? refs.stream().reduce("", (a, b) -> b) : last;
    }

    /**
     * Canonical extractor: every referenced type name, including generic
     * arguments. {@code "Map<String, List<UUID>>"} -> {@code [Map, String, List, UUID]}.
     */
    public static Set<String> referencedTypeNames(String rawType) {
        Set<String> names = new LinkedHashSet<>();
        if (rawType == null || rawType.isBlank()) {
            return names;
        }
        for (String token : rawType.split("[^A-Za-z0-9_.]+")) {
            if (token.isBlank()) {
                continue;
            }
            String simple = token.contains(".") ? token.substring(token.lastIndexOf('.') + 1) : token;
            simple = simple.replace("[]", "").trim();
            if (!simple.isEmpty() && Character.isUpperCase(simple.charAt(0))) {
                names.add(simple);
            }
        }
        return names;
    }

    public static String resolvePackage(String typeName) {
        if (typeName == null || typeName.isBlank()) {
            return "(unresolved)";
        }
        if (COMMON_JDK_TYPES.containsKey(typeName)) {
            return COMMON_JDK_TYPES.get(typeName);
        }
        if (typeName.contains(".")) {
            return typeName.substring(0, typeName.lastIndexOf('.'));
        }
        // Unknown simple name: caller should prefer file imports; java.lang
        // is only a heuristic for JDK scalars, not a fact.
        return "java.lang";
    }
}
