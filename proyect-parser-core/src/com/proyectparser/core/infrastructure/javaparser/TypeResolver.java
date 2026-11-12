package com.proyectparser.core.infrastructure.javaparser;

import com.proyectparser.core.domain.model.ClassModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Resuelve una referencia a tipo escrita en una clase ({@code Foo}, {@code h.b.Foo},
 * {@code Outer.Inner}) a una clase interna (su fqn) o a una externa (su paquete).
 * Lo comparten {@link RelationshipDetector} y {@link ExternalTypeRegistrar}.
 *
 * <p>Prioriza nombres calificados, alcance léxico, imports y paquete local.
 * Para fuentes parciales conserva la heurística de nombre interno único;
 * los homónimos ambiguos no se enlazan.
 */
class TypeResolver {

    /** Destino de una referencia: una clase interna o una externa con su paquete. */
    record Target(String internalFqn, String externalPackage, boolean heuristicPackage) {
        boolean isInternal() {
            return internalFqn != null;
        }
    }

    private final Map<String, List<String>> internalFqnsByName = new HashMap<>();
    private final Set<String> internalFqns = new HashSet<>();
    // fqn de la clase -> (simpleName -> package), según el archivo donde se declaró.
    private final Map<String, Map<String, String>> fileImportsByClass;

    TypeResolver(List<ClassModel> internals, Map<String, Map<String, String>> fileImportsByClass) {
        for (ClassModel model : internals) {
            if (internalFqns.add(model.getFqn())) {
                internalFqnsByName.computeIfAbsent(model.getName(), k -> new ArrayList<>()).add(model.getFqn());
            }
        }
        this.fileImportsByClass = fileImportsByClass;
    }

    /** Fqn de la clase interna a la que apunta {@code reference}, o null. */
    String internal(ClassModel from, String reference) {
        Target target = resolve(from, reference);
        return target != null && target.isInternal() ? target.internalFqn() : null;
    }

    /** Destino de {@code reference} escrita en {@code from}; null si es ambigua. */
    Target resolve(ClassModel from, String reference) {
        String name = GenericTypeParser.simpleName(reference);
        List<String> sameName = internalFqnsByName.getOrDefault(name, List.of());
        Map<String, String> imports = fileImportsByClass.getOrDefault(from.getFqn(), Map.of());
        String imported = imports.get(name);
        // 1. Nombre calificado tal como se escribió.
        if (reference.contains(".")) {
            return qualified(from, reference);
        }
        // 2. Tipo anidado de la clase o de sus contenedoras (p.A.B -> p.A.B.X, p.A.X).
        String pkg = from.getPackageName() == null ? "" : from.getPackageName();
        String scope = from.getFqn();
        while (scope.length() > pkg.length()) {
            if (internalFqns.contains(scope + "." + name)) {
                return internalTarget(scope + "." + name);
            }
            scope = scope.substring(0, Math.max(scope.lastIndexOf('.'), 0));
        }
        // 3. Import explícito del archivo.
        if (imported != null) {
            String fqn = imported + "." + name;
            return internalFqns.contains(fqn) ? internalTarget(fqn) : new Target(null, imported, false);
        }
        // 4. Mismo paquete.
        String local = pkg.isEmpty() ? name : pkg + "." + name;
        if (internalFqns.contains(local)) {
            return internalTarget(local);
        }
        List<String> wildcardPackages = imports.entrySet().stream()
                .filter(entry -> entry.getKey().endsWith(".*"))
                .map(Map.Entry::getValue).sorted().toList();
        List<String> candidates = wildcardPackages.stream().map(p -> p + "." + name)
                .filter(internalFqns::contains).toList();
        if (candidates.size() == 1) {
            return internalTarget(candidates.get(0));
        }
        if (candidates.size() > 1) {
            return null;
        }
        // java.lang se importa implícitamente: un comodín ajeno no lo desplaza.
        if (TypeClassifier.isJavaLangType(name)) {
            return new Target(null, "java.lang", true);
        }
        if (wildcardPackages.size() == 1) {
            // Known JDK types keep their package when another wildcard is unrelated.
            String jdk = TypeClassifier.resolvePackage(name);
            String external = wildcardPackages.contains(jdk) ? jdk : wildcardPackages.get(0);
            return new Target(null, external, true);
        }
        return sameName.size() == 1 ? internalTarget(sameName.get(0))
                : sameName.isEmpty() ? new Target(null, TypeClassifier.resolvePackage(name), true) : null;
    }

    /** {@code h.b.Foo} / {@code java.sql.Date} (paquete escrito) u {@code Outer.Inner} (tipo escrito). */
    private Target qualified(ClassModel from, String reference) {
        if (internalFqns.contains(reference)) {
            return internalTarget(reference);
        }
        String qualifier = reference.substring(0, reference.lastIndexOf('.'));
        int dot = reference.indexOf('.');
        String head = reference.substring(0, dot);
        if (head.isEmpty() || !Character.isUpperCase(head.charAt(0))) {
            return new Target(null, qualifier, false);
        }
        Target outer = resolve(from, head);
        if (outer == null) {
            return null;
        }
        if (outer.isInternal()) {
            String fqn = outer.internalFqn() + reference.substring(dot);
            return internalFqns.contains(fqn) ? internalTarget(fqn) : null;
        }
        return new Target(null, outer.externalPackage() + "." + qualifier, outer.heuristicPackage());
    }

    private static Target internalTarget(String fqn) {
        return new Target(fqn, null, false);
    }
}
