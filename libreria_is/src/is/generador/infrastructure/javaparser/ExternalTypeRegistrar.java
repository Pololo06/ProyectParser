package is.generador.infrastructure.javaparser;

import is.generador.domain.model.AttributeModel;
import is.generador.domain.model.ClassModel;
import is.generador.domain.model.RelationshipModel;
import is.generador.domain.policy.DiagramFilter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Registra tipos externos (paso 6 del split).
 * Crea cajas {@code @external} con su paquete JDK real y una
 * ASSOCIATION desde la clase que los usa. Internos, primitivos,
 * escalares y variables de tipo se ignoran. Opción B: la whitelist
 * es solo para clases internas; las externas solo obedecen a blacklist.
 */
class ExternalTypeRegistrar {

    private final DiagramFilter filter;

    ExternalTypeRegistrar(DiagramFilter filter) {
        this.filter = filter != null ? filter : new DiagramFilter();
    }

    /**
     * Registers external (non-project, non-primitive) attribute types as
     * stereotyped {@code @external} boxes with their real JDK package and an
     * ASSOCIATION from the using class. Internal types and primitives are ignored.
     */
    void registerExternalTypes(List<ClassModel> classes, List<RelationshipModel> relationships,
            Map<String, Map<String, String>> fileImportsByClass) {
        Set<String> internal = new HashSet<>();
        for (ClassModel model : classes) {
            internal.add(model.getName());
        }
        Set<String> seen = new HashSet<>();
        for (RelationshipModel rel : relationships) {
            seen.add(rel.getSource() + "|" + rel.getType() + "|" + rel.getTarget());
        }
        Map<String, ClassModel> externals = new LinkedHashMap<>();
        for (ClassModel model : classes) {
            if (externals.containsKey(model.getName())) {
                continue; // skip boxes created in this same pass
            }
            if (model.getAttributes() == null) {
                continue;
            }
            for (AttributeModel attribute : model.getAttributes()) {
                // 1. Omitir primitivos y escalares básicos (String, wrappers, etc.)
                if (TypeClassifier.shouldIgnoreBox(attribute.getType())) {
                    continue;
                }
                // 2. Registrar TODOS los tipos referenciados (incluye genéricos
                //    anidados: Map<String,List<UUID>> -> UUID, no solo el último),
                //    salvo variables de tipo declaradas (<T, ID>).
                Set<String> candidates = GenericTypeParser.visibleTypeNames(
                        TypeClassifier.referencedTypeNames(attribute.getType()),
                        model.getTypeParameters());
                for (String target : candidates) {
                    if (TypeClassifier.shouldIgnoreBox(target)) {
                        continue;
                    }
                    if (internal.contains(target)) {
                        continue;
                    }
                if (!externals.containsKey(target)) {
                    // 2. Prioridad: imports del archivo > mapa común > fallback
                    String resolvedPkg = fileImportsByClass
                            .getOrDefault(model.getName(), Map.of())
                            .getOrDefault(target, TypeClassifier.resolvePackage(target));
                    // 3. La caja externa solo se crea si no está en blacklist
                    // (opción B: la whitelist es solo para clases internas).
                    if (filter.isBlacklisted(resolvedPkg, target)) {
                        continue;
                    }
                    List<String> stereotypes = new ArrayList<>();
                    stereotypes.add("@external");
                    externals.put(target, new ClassModel(target,
                            resolvedPkg, "Class", false,
                            stereotypes, new ArrayList<>(), new ArrayList<>(),
                            new ArrayList<>(), new ArrayList<>(), new ArrayList<>(),
                            new ArrayList<>(), List.of()));
                    }
                    addOnce(relationships, seen, model.getName(), target, "ASSOCIATION");
                }
            }
        }
        classes.addAll(externals.values());
    }

    private void addOnce(List<RelationshipModel> relationships, Set<String> seen,
                         String source, String target, String type) {
        String key = source + "|" + type + "|" + target;
        if (seen.add(key)) {
            relationships.add(new RelationshipModel(source, target, type));
        }
    }
}
