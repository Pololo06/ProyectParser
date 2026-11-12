package com.proyectparser.core.infrastructure.javaparser;

import com.proyectparser.core.domain.model.AttributeModel;
import com.proyectparser.core.domain.model.ClassModel;
import com.proyectparser.core.domain.model.ConstructorModel;
import com.proyectparser.core.domain.model.Kind;
import com.proyectparser.core.domain.model.MethodModel;
import com.proyectparser.core.domain.model.ParameterModel;
import com.proyectparser.core.domain.model.RelType;
import com.proyectparser.core.domain.model.RelationshipModel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Registra tipos externos (paso 6 del split).
 * Crea cajas {@code @external} con su paquete JDK real y una
 * DEPENDENCY desde la clase que los usa. Internos, primitivos,
 * escalares y variables de tipo se ignoran. El filtrado lo hace
 * {@code ProjectFilter}.
 */
class ExternalTypeRegistrar {

    /**
     * Registers external types used in attributes and method signatures as
     * stereotyped {@code @external} boxes and dependencies from the using class.
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
            Set<String> references = new LinkedHashSet<>();
            for (AttributeModel attribute : model.getAttributes()) {
                references.addAll(GenericTypeParser.visibleReferences(attribute.getType(),
                        model.getTypeParameters()));
            }
            for (MethodModel method : model.getMethods()) {
                addSignatureReferences(references, model, method, method.getReturnType());
                for (ParameterModel parameter : method.getParameters()) {
                    addSignatureReferences(references, model, method, parameter.getType());
                }
            }
            for (ConstructorModel constructor : model.getConstructors()) {
                for (ParameterModel parameter : constructor.getParameters()) {
                    addSignatureReferences(references, model, constructor.getTypeParameters(), parameter.getType());
                }
            }
            for (String reference : references) {
                String target = GenericTypeParser.simpleName(reference);
                if (TypeClassifier.shouldIgnoreBox(target)) {
                    continue;
                }
                TypeResolver.Target resolved = resolver.resolve(model, reference);
                if (resolved == null || resolved.isInternal()) {
                    continue;
                }
                ClassModel box = box(externals, heuristicBoxes, target, resolved);
                RelationshipDetector.addOnce(relationships, seen, model.getFqn(), box.getFqn(),
                        RelType.DEPENDENCY);
            }
        }
        classes.addAll(externals.values());
    }

    private static void addSignatureReferences(Set<String> references, ClassModel model,
            MethodModel method, String type) {
        addSignatureReferences(references, model, method.getTypeParameters(), type);
    }

    private static void addSignatureReferences(Set<String> references, ClassModel model,
            List<String> typeParameters, String type) {
        Set<String> signature = GenericTypeParser.signatureReferences(type, model.getTypeParameters(),
                typeParameters);
        // Generic wrappers in signatures do not introduce an external box by themselves.
        int arguments = type.indexOf('<');
        if (arguments >= 0) {
            signature.remove(type.substring(0, arguments).strip());
        }
        references.addAll(signature);
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
