package is.generador.infrastructure.javaparser;

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
     * Every type referenced in a type string, as written ({@code h.b.Foo}, {@code Outer.Inner})
     * so it can be resolved, including generic arguments, minus in-scope type variables
     * (e.g. {@code T}, {@code ID} from {@code class Repo<T, ID>}); type variables are never
     * real relations. Examples: "List&lt;Package&gt;" -&gt; {List, Package};
     * "Map&lt;String, List&lt;h.b.SubZone&gt;&gt;" -&gt; {Map, String, List, h.b.SubZone}.
     */
    static Set<String> visibleReferences(String typeString, List<String> excluded) {
        return visibleReferences(typeString, excluded, List.of());
    }

    static Set<String> visibleReferences(String typeString, List<String> first, List<String> second) {
        Set<String> references = TypeClassifier.referencedTypes(typeString);
        references.removeIf(reference -> {
            String name = simpleName(reference);
            return (first != null && first.contains(name)) || (second != null && second.contains(name));
        });
        return references;
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
        cleaned = cleaned.replace("...", "").trim();
        return cleaned;
    }
}
