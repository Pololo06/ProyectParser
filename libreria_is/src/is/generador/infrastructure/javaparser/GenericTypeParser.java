package is.generador.infrastructure.javaparser;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Pure string-level parsing of Java type expressions (paso 1 del split).
 * No JavaParser types here on purpose: balanced {@code < >} counting,
 * name extraction and type-variable exclusion over raw type strings.
 */
class GenericTypeParser {

    private GenericTypeParser() {
    }

    /**
     * Extracts every referenced type name from a type string, including generic arguments.
     * Examples: "List&lt;Package&gt;" -&gt; {List, Package};
     * "Map&lt;String, List&lt;SubZone&gt;&gt;" -&gt; {Map, String, List, SubZone}.
     */
    static Set<String> extractReferencedNames(String typeString) {
        return visibleTypeNames(extractAllNames(typeString), null);
    }

    /**
     * Referenced names minus in-scope type variables (e.g. {@code T}, {@code ID}
     * from {@code class Repo<T, ID>}); type variables are never real relations.
     */
    static Set<String> visibleTypeNames(Set<String> names, List<String> excluded) {
        if (excluded != null && !excluded.isEmpty()) {
            names.removeAll(excluded);
        }
        return names;
    }

    static Set<String> visibleTypeNames(Set<String> names, List<String> first, List<String> second) {
        visibleTypeNames(names, first);
        return visibleTypeNames(names, second);
    }

    static Set<String> splitTypeNames(String typeString) {
        return extractReferencedNames(typeString);
    }

    private static Set<String> extractAllNames(String typeString) {        Set<String> names = new LinkedHashSet<>();
        if (typeString == null || typeString.isBlank()) {
            return names;
        }
        // split on anything that is not a Java identifier part
        for (String token : typeString.split("[^A-Za-z0-9_.]+")) {
            if (token.isBlank()) {
                continue;
            }
            String simple = simpleName(token);
            if (!simple.isEmpty() && Character.isUpperCase(simple.charAt(0))) {
                names.add(simple);
            }
        }
        return names;
    }

    static String simpleName(String typeName) {
        if (typeName == null) {
            return "";
        }
        // Strip generic arguments with balanced depth counting, so nested
        // generics like Map<String, List<SubZone>> resolve to "Map" without
        // the greedy-regex pitfall of "<.*>" eating too much or too little.
        StringBuilder outer = new StringBuilder();
        int depth = 0;
        for (int i = 0; i < typeName.length(); i++) {
            char c = typeName.charAt(i);
            if (c == '<') {
                depth++;
            } else if (c == '>') {
                if (depth > 0) {
                    depth--;
                }
            } else if (depth == 0) {
                outer.append(c);
            }
        }
        String cleaned = outer.toString().trim();
        int dot = cleaned.lastIndexOf('.');
        if (dot >= 0) {
            cleaned = cleaned.substring(dot + 1);
        }
        // strip array brackets and varargs
        cleaned = cleaned.replace("[]", "").replace("...", "").trim();
        return cleaned;
    }
}
