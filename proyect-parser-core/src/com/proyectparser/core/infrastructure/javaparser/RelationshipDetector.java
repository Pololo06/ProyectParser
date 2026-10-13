package com.proyectparser.core.infrastructure.javaparser;

import com.proyectparser.core.domain.model.AttributeModel;
import com.proyectparser.core.domain.model.ClassModel;
import com.proyectparser.core.domain.model.ConstructorModel;
import com.proyectparser.core.domain.model.MethodModel;
import com.proyectparser.core.domain.model.ParameterModel;
import com.proyectparser.core.domain.model.RelType;
import com.proyectparser.core.domain.model.RelationshipModel;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Deduce relaciones del modelo (paso 5 del split).
 * Solo lectura del AST ya convertido: EXTENDS, IMPLEMENTS,
 * ASSOCIATION (atributos, con genéricos), DEPENDENCY (firmas) y NESTING
 * (tipo anidado hacia su tipo contenedor directo).
 * Las variables de tipo declaradas nunca generan relaciones.
 * Los extremos son fqn; {@code ProjectAnalyzer} los traduce a ids al final.
 */
class RelationshipDetector {

    void detectRelationships(List<ClassModel> classes, List<RelationshipModel> relationships,
                             TypeResolver resolver) {
        Set<String> seen = new HashSet<>();
        Set<String> fqns = new HashSet<>();
        for (ClassModel model : classes) {
            fqns.add(model.getFqn());
        }

        for (ClassModel model : classes) {
            String self = model.getFqn();
            String enclosing = enclosingFqn(model);
            if (enclosing != null && fqns.contains(enclosing)) {
                addOnce(relationships, seen, self, enclosing, RelType.NESTING);
            }
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

    /**
     * Fqn of the type that directly encloses a nested type ({@code p.Outer} for
     * {@code p.Outer.Inner}); {@code null} for a top-level or local type.
     */
    static String enclosingFqn(ClassModel model) {
        String pkg = model.getPackageName() == null ? "" : model.getPackageName();
        String topLevel = pkg.isEmpty() ? model.getName() : pkg + "." + model.getName();
        String fqn = model.getFqn();
        if (fqn == null || fqn.equals(topLevel) || !fqn.endsWith("." + model.getName())) {
            return null;
        }
        return fqn.substring(0, fqn.length() - model.getName().length() - 1);
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
