package is.generador.infrastructure.javaparser;

import is.generador.domain.model.AttributeModel;
import is.generador.domain.model.ClassModel;
import is.generador.domain.model.Kind;
import is.generador.domain.model.RelType;
import is.generador.domain.model.RelationshipModel;

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
 * escalares y variables de tipo se ignoran. El filtrado lo hace
 * {@code ProjectFilter}.
 */
class ExternalTypeRegistrar {

    /**
     * Registers external (non-project, non-primitive) attribute types as
     * stereotyped {@code @external} boxes with their real JDK package and an
     * ASSOCIATION from the using class. Internal types and primitives are ignored.
     *
     * <p>A box is keyed by package + name. Two externals with the same simple name are
     * only kept apart when both packages come from an explicit import or a qualified
     * name; if one comes from the heuristic, the first box wins, as before.
     */
    void registerExternalTypes(List<ClassModel> classes, List<RelationshipModel> relationships,
            TypeResolver resolver) {
        Set<String> seen = new HashSet<>();
        for (RelationshipModel rel : relationships) {
            seen.add(rel.key());
        }
        Map<String, ClassModel> externals = new LinkedHashMap<>();   // fqn -> caja
        Set<String> heuristicBoxes = new HashSet<>();                // fqn de las cajas con paquete heurístico
        for (ClassModel model : classes) {
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
                for (String reference : GenericTypeParser.visibleReferences(attribute.getType(),
                        model.getTypeParameters())) {
                    String target = GenericTypeParser.simpleName(reference);
                    if (TypeClassifier.shouldIgnoreBox(target)) {
                        continue;
                    }
                    // 3. Paquete: import del archivo o nombre calificado > mapa común > fallback
                    TypeResolver.Target resolved = resolver.resolve(model, reference);
                    if (resolved == null || resolved.isInternal()) {
                        continue;
                    }
                    ClassModel box = box(externals, heuristicBoxes, target, resolved);
                    RelationshipDetector.addOnce(relationships, seen, model.getFqn(), box.getFqn(),
                            RelType.ASSOCIATION);
                }
            }
        }
        classes.addAll(externals.values());
    }

    /** The box for {@code name} in the resolved package, created on first use. */
    private static ClassModel box(Map<String, ClassModel> externals, Set<String> heuristicBoxes,
            String name, TypeResolver.Target resolved) {
        List<String> stereotypes = new ArrayList<>();
        stereotypes.add(ClassModel.EXTERNAL_STEREOTYPE);
        ClassModel candidate = new ClassModel(name,
                resolved.externalPackage(), Kind.CLASS.label(), false,
                stereotypes, new ArrayList<>(), new ArrayList<>(),
                new ArrayList<>(), new ArrayList<>(), new ArrayList<>(),
                new ArrayList<>(), List.of());
        ClassModel same = externals.get(candidate.getFqn());
        if (same != null) {
            return same;
        }
        for (ClassModel box : externals.values()) {
            if (box.getName().equals(name)
                    && (resolved.heuristicPackage() || heuristicBoxes.contains(box.getFqn()))) {
                return box;
            }
        }
        externals.put(candidate.getFqn(), candidate);
        if (resolved.heuristicPackage()) {
            heuristicBoxes.add(candidate.getFqn());
        }
        return candidate;
    }
}
