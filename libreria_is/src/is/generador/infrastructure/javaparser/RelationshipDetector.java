package is.generador.infrastructure.javaparser;

import is.generador.domain.model.AttributeModel;
import is.generador.domain.model.ClassModel;
import is.generador.domain.model.ConstructorModel;
import is.generador.domain.model.MethodModel;
import is.generador.domain.model.ParameterModel;
import is.generador.domain.model.RelType;
import is.generador.domain.model.RelationshipModel;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Deduce relaciones del modelo (paso 5 del split).
 * Solo lectura del AST ya convertido: EXTENDS, IMPLEMENTS,
 * ASSOCIATION (atributos, con genéricos) y DEPENDENCY (firmas).
 * Las variables de tipo declaradas nunca generan relaciones.
 * Los extremos son fqn; {@code ProjectAnalyzer} los traduce a ids al final.
 */
class RelationshipDetector {

    void detectRelationships(List<ClassModel> classes, List<RelationshipModel> relationships,
                             TypeResolver resolver) {
        Set<String> seen = new HashSet<>();

        for (ClassModel model : classes) {
            String self = model.getFqn();
            for (String parent : model.getExtendedTypes()) {
                String parentFqn = resolver.internal(model, GenericTypeParser.simpleName(parent));
                if (parentFqn != null) {
                    addOnce(relationships, seen, self, parentFqn, RelType.EXTENDS);
                }
                // generics inside extends clause, e.g. extends Base<Package>
                for (String inner : GenericTypeParser.visibleReferences(parent, model.getTypeParameters())) {
                    String target = resolver.internal(model, inner);
                    if (target != null && !target.equals(self) && !target.equals(parentFqn)) {
                        addOnce(relationships, seen, self, target, RelType.ASSOCIATION);
                    }
                }
            }
            for (String parent : model.getImplementedTypes()) {
                String parentFqn = resolver.internal(model, GenericTypeParser.simpleName(parent));
                if (parentFqn != null) {
                    addOnce(relationships, seen, self, parentFqn, RelType.IMPLEMENTS);
                }
                for (String inner : GenericTypeParser.visibleReferences(parent, model.getTypeParameters())) {
                    String target = resolver.internal(model, inner);
                    if (target != null && !target.equals(self) && !target.equals(parentFqn)) {
                        addOnce(relationships, seen, self, target, RelType.ASSOCIATION);
                    }
                }
            }

            // ASSOCIATION: attribute types (including generic arguments like List<Package>)
            Set<String> associated = new HashSet<>();
            for (AttributeModel attribute : model.getAttributes()) {
                for (String reference : GenericTypeParser.visibleReferences(attribute.getType(),
                        model.getTypeParameters())) {
                    String target = resolver.internal(model, reference);
                    if (target != null && !target.equals(self)) {
                        addOnce(relationships, seen, self, target, RelType.ASSOCIATION);
                        associated.add(target);
                    }
                }
            }

            // DEPENDENCY: types used only in method/constructor signatures (params, returns)
            // or record components already covered as attributes are skipped.
            for (MethodModel method : model.getMethods()) {
                addDependencies(relationships, seen, resolver, model, associated, method.getReturnType(),
                        method.getTypeParameters());
                for (ParameterModel parameter : method.getParameters()) {
                    addDependencies(relationships, seen, resolver, model, associated, parameter.getType(),
                            method.getTypeParameters());
                }
            }
            for (ConstructorModel constructor : model.getConstructors()) {
                for (ParameterModel parameter : constructor.getParameters()) {
                    addDependencies(relationships, seen, resolver, model, associated, parameter.getType(),
                            constructor.getTypeParameters());
                }
            }
        }
    }

    private static void addDependencies(List<RelationshipModel> relationships, Set<String> seen,
                                        TypeResolver resolver, ClassModel model, Set<String> associated,
                                        String type, List<String> memberTypeParameters) {
        for (String reference : GenericTypeParser.visibleReferences(type, model.getTypeParameters(),
                memberTypeParameters)) {
            String target = resolver.internal(model, reference);
            if (target != null && !target.equals(model.getFqn()) && !associated.contains(target)) {
                addOnce(relationships, seen, model.getFqn(), target, RelType.DEPENDENCY);
            }
        }
    }

    static void addOnce(List<RelationshipModel> relationships, Set<String> seen,
                        String source, String target, RelType type) {
        RelationshipModel rel = new RelationshipModel(source, target, type.label());
        if (seen.add(rel.key())) {
            relationships.add(rel);
        }
    }
}
